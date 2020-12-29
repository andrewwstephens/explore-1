// Copyright (c) 2016-2020 Association of Universities for Research in Astronomy, Inc. (AURA)
// For license information see LICENSE or https://opensource.org/licenses/BSD-3-Clause

package explore.components

import cats._
import cats.data.Validated._
import cats.syntax.all._
import cats.effect.IO
import crystal.ViewF
import crystal.react.implicits._
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string._
import explore.AppCtx
import explore._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.MonocleReact._
import lucuma.core.util.Display
import lucuma.core.util.Enumerated
import lucuma.ui.forms._
import lucuma.ui.optics._
import monocle.Lens
import monocle.macros.Lenses
import org.scalajs.dom.document
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.html
import react.semanticui.elements.button.Button
import reactST.agGridCommunity.colDefMod._
import reactST.agGridCommunity.iCellEditorMod.ICellEditorParams
import reactST.agGridCommunity.iCellRendererMod.ICellRendererParams
import reactST.agGridReact.components._

import scalajs.js

object AgGridHelpers {

  /**
   * Returns a display only AgGridColumn Builder.
   * Since this returns a Builder, additional column configuration methods can
   * be chained onto the return value.
   *
   * @param lens A Lens to go between the Row value and the Cell value.
   * @param formatter Function for formatting the value. This will often be the `reverseGet` of a Format or ValidFormatInput.
   * @return An AgGridColumn Builder.
   */
  def viewColumn[A, B](lens: Lens[A, B])(formatter: B => String): AgGridColumn.ColDef.Builder =
    AgGridColumn.ColDef
      .valueGetter(getter(lens))
      .cellRendererFramework(renderer(formatter))

  /**
   * Create a AgGridColumn Builder for a column which edits the value in a FormInputEv.
   * Since this returns a Builder, additional column configuration methods can
   * be chained onto the return value.
   *
   * @param lens Lens to go between the Row value and the Cell value.
   * @param validFormat The ValidFormatInput for the editor.
   * @param changeAuditor The ChangeAuditor for the editor.
   * @param disabled Whether editing should be disabled or not.
   * @return An AgGridColumn Builder
   */
  def editableViewColumn[A, B](
    lens:          Lens[A, B],
    validFormat:   ValidFormatInput[B],
    changeAuditor: ChangeAuditor[B] = ChangeAuditor.accept[B],
    disabled:      Boolean = false
  )(implicit eq:   Eq[B]): AgGridColumn.ColDef.Builder =
    AgGridColumn.ColDef
      .editable(!disabled)
      .valueGetter(getter(lens))
      .valueSetter(noopSetter)
      .cellRendererFramework(renderer(validFormat.reverseGet))
      .cellEditorFramework(makeFormInputEVEditor(validFormat, changeAuditor))
      .suppressKeyboardEvent(suppressKeys)

  /**
   * Create a AgGridColumn Builder for a column which edits the value via a EnumViewSelect.
   * Obviously, the cell value must have an Enumerated instance.
   * Since this returns a Builder, additional column configuration methods can
   * be chained onto the return value.
   *
   * @param lens Lens to go between the Row value and the Cell value.
   * @param disabled Whether editing should be disabled or not.
   * @param exclude A Set of values to exclude from the dropdown list.
   * @param includeCurrent Used with `exclude` to always include the currently selected value in the dropdown list, even if it is in the exclude set.
   * @return An AgGridColumn Builder.
   */
  def editableEnumViewColumn[A, B](
    lens:           Lens[A, B],
    disabled:       Boolean = false,
    exclude:        Set[B] = Set.empty[B],
    includeCurrent: Boolean = false
  )(implicit enum:  Enumerated[B], display: Display[B]): AgGridColumn.ColDef.Builder =
    AgGridColumn.ColDef
      .editable(!disabled)
      .valueGetter(getter(lens))
      .valueSetter(noopSetter)
      .cellRendererFramework(renderer[B](display.shortName))
      .cellEditorFramework(makeFormInputEnumEditor(exclude, includeCurrent))
      .suppressKeyboardEvent(suppressKeys)

  /**
   * Create a column containing a button.
   * The button is passed in to allow maximum flexibility, but the
   * onClick handler should not be specified for the button, since
   * it will be added here.
   *
   * The [A] type is the type of the entire data row.
   *
   * @param button The button to put in the cell.
   * @param onClick The onClick handler for the button.
   * @param disabled Whether the button should be disabled.
   * @return An AgGridColumn Builder.
   */
  def buttonViewColumn[A](
    button:   Button,
    onClick:  A => Callback,
    disabled: Boolean = false
  ): AgGridColumn.ColDef.Builder = {
    val component = ScalaComponent
      .builder[View[A]]
      .render_P { view =>
        button.addModifiers(Seq(^.onClick ==> (_ => onClick(view.get)), ^.disabled := disabled))
      }
      .build
      .cmapCtorProps[ICellEditorParams](parms => parms.value.asInstanceOf[View[A]])
      .toJsComponent
      .raw

    def getter: js.Function1[ValueGetterParams, js.Any] = parms => parms.data
    AgGridColumn.ColDef.valueGetter(getter).cellRendererFramework(component)
  }

  // We return a View into the column
  private def getter[A, B](lens: Lens[A, B]): js.Function1[ValueGetterParams, js.Any] =
    parms => parms.data.asInstanceOf[View[A]].zoom(lens).asInstanceOf[js.Any]

  // We use the View to set the values, not the grid functions.
  // Return Boolean would prevent a re-render, but I don't think that's necessary if we
  // use the isCancelAfterEnd method in the editing component to just cancel if the value
  // is invalid.
  private val noopSetter: js.Function1[ValueSetterParams, Boolean] = _ => true

  // This receives a View[A] in the Props.
  private def renderer[A](formatter: A => String) = ScalaComponent
    .builder[View[A]]
    .render_P((props: View[A]) => <.span(formatter(props.get)))
    .build
    .cmapCtorProps[ICellRendererParams](p => p.value.asInstanceOf[View[A]])
    .toJsComponent
    .raw

  private val allowedKeys = List(KeyCode.Escape, KeyCode.Enter, KeyCode.Tab)

  private def suppressKeys(parms: SuppressKeyboardEventParams): Boolean =
    parms.editing && !allowedKeys.contains(parms.event.keyCode)

  private def newId: NonEmptyString = NonEmptyString
    .from("Id" + java.util.UUID.randomUUID().toString().replace("-", ""))
    .getOrElse("idddd")

  // TODO: Need to make a Backend for this to expose the ag-grid lifecycle methods
  // (if that's how we expose them). Also probably want to edit State instead of view
  // directly, as with the FormInputEV version.
  private def makeFormInputEnumEditor[A](exclude: Set[A], includeCurrent: Boolean)(implicit
    enum:                                         Enumerated[A],
    display:                                      Display[A]
  ) = {
    val excludeFn: A => Set[A] = a => if (includeCurrent) exclude - a else exclude
    ScalaComponent
      .builder[View[A]]
      .render_P(view => EnumViewSelect(id = newId, value = view, exclude = excludeFn(view.get)))
      .build
      .cmapCtorProps[ICellEditorParams](parms => parms.value.asInstanceOf[View[A]])
      .toJsComponent
      .raw
  }

  private def makeFormInputEVEditor[A](
    validator:     ValidFormatInput[A],
    changeAuditor: ChangeAuditor[A]
  )(implicit eq:   Eq[A]) = {

    @Lenses
    case class State(original: A, current: A, editing: A)

    class Backend($ : BackendScope[View[A], State])(implicit val eq: Eq[A]) {
      val id = newId

      // We need to add a little delay before looking for the input element. I think
      // ag-grid is using some virtualization of it's own, and the input is not in
      // the DOM immediately.
      def setCursor(): Callback =
        CallbackTo {
          val inputOpt = Option(document.querySelector(s"#${id.value}").asInstanceOf[html.Input])
          inputOpt.map { i =>
            i.focus();
            val len = i.value.length
            i.setSelectionRange(0, len)
          }
        }.void.delayMs(1).toCallback

      // We have to wait until getValue is called before setting the value or
      // we get "kicked out of" editing due to re-rendering of the table.
      // So, we need to `createInitialStateFromProps` to create a View
      // in the State that can be edited by the FormInputEv. We also
      // keep the original value so that we can compare and return the
      // appropriate response in the `isCancelAfterEnd` "lifecycle" method.

      def onTextChange(text: String): Callback =
        validator.getValidated(text) match {
          case Valid(a)   => $.setStateL(State.current)(a)
          // If the current text is invalid, we reset to the original to behave more
          // like the inputs that are not in tables.
          case Invalid(_) => $.state.flatMap(s => $.setStateL(State.current)(s.original))
        }

      // ag-grid lifecycle method.
      // Note that it doesn't matter what `getValue` actually returns, since
      // 'valueSetter' is a noop. We just need to set the value in the View
      // in the props.
      // Is there a way to avoid calling runNow()?
      def getValue(): js.Any = $.state.map(_.current).runNow().asInstanceOf[js.Any]

      // ag-grid lifecycle method.
      // If the value is unchanged, we'll cancel to avoid a table re-render.
      // Is there a way to avoid calling runNow()?
      def isCancelAfterEnd(): Boolean = $.state.map(s => s.current === s.original).runNow()

      def render() =
        AppCtx.withCtx { implicit ctx =>
          implicit val cs = ctx.cs

          val value = ViewF.fromState[IO]($).zoom(State.editing)
          FormInputEV[View, A](id = id,
                               value = value,
                               validFormat = validator,
                               changeAuditor = changeAuditor,
                               onTextChange = onTextChange
          )
        }
    }

    ScalaComponent
      .builder[View[A]]
      .initialStateFromProps(p => State(p.get, p.get, p.get))
      .backend(new Backend(_))
      .renderBackend
      .componentDidMount(_.backend.setCursor())
      .build
      .cmapCtorProps[ICellEditorParams](parms => parms.value.asInstanceOf[View[A]])
      .toJsComponent
      .raw
  }
}
