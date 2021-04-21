package explore.config

import cats.syntax.all._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import react.common._
import explore.common.SequenceStepsGQL.SequenceSteps.Data.Observations.Nodes.Config
import reactST.reactTable.TableMaker

import scalajs.js.JSConverters._
import react.semanticui.elements.segment.Segment

final case class SequenceTable(config: Config)
    extends ReactProps[SequenceTable](SequenceTable.component)

object SequenceTable {
  type Props = SequenceTable

  type Step = Config.GmosSouthConfig.Science.Atoms.Steps

  private case class StepLine(step: Step, firstOf: Option[Int])

  val component =
    ScalaComponent
      .builder[Props]
      .render_P { props =>
        val tableMaker = TableMaker[StepLine]
        // import tableMaker.syntax._

        val columns = tableMaker.columnArray(
          tableMaker
            .accessorColumn("atomSteps", _.firstOf.map(_.toString).getOrElse(""))
            .setHeader(" "),
          tableMaker
            .accessorColumn("stepType", _.step.stepType.toString)
            .setHeader("Step Type"),
          tableMaker
            .accessorColumn("time", _.step.time.total.duration.toString)
            .setHeader("Time")
        )

        val options = tableMaker
          .options(rowIdFn = _.step.time.total.duration.toString, columns = columns)
        // .setInitialStateFull(tableState)

        // <use xmlns="http://www.w3.org/2000/svg" transform="matrix(-1,0,0,1,260,0)" id="use8" x="0" y="0" width="260" height="1030" xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="#llv"/>
        // <path xmlns="http://www.w3.org/2000/svg" d="m 177,430 5,0 c 6,0 10,0 14,-1 4,-1 8,-4 11,-8 3,-4 4,-8 5,-13 1,-5 1,-14 1,-26 0,-9 0,-15 1,-20 1,-5 3,-10 6,-13 3,-3 7,-5 13,-5 l 0,-16 c -6,0 -10,-2 -13,-5 -3,-3 -5,-8 -6,-13 -1,-5 -1,-11 -1,-20 0,-12 0,-21 -1,-26 -1,-5 -2,-9 -5,-13 -3,-4 -7,-7 -11,-8 -4,-1 -8,-1 -14,-1 l -5,0 0,15 3,0 c 7,0 11,1 13,3 3,3 4,4 4,7 l 0,25 c 0,15 2,25 5,31 3,6 9,10 15,13 -6,3 -12,7 -15,13 -3,6 -5,16 -5,31 l 0,25 c 0,3 -1,4 -4,7 -2,2 -6,3 -13,3 l -3,0 0,15 z" id="llv"/>

        Segment()(
          props.config match {
            case Config.GmosSouthConfig(_, _, science) =>
              val steps = science.atoms
                .map(atom =>
                  atom.steps.headOption.map(head => StepLine(head, atom.steps.length.some)) ++
                    atom.steps.tail.map(s => StepLine(s, none))
                )
                .flatten

              println(steps)
              println(steps.length)

              tableMaker.makeTable(
                options = options,
                data = steps.toJSArray,
                headerCellFn = (c => TableMaker.basicHeaderCellFn(Css.Empty)(c)).some,
                tableClass = Css("ui very celled selectable striped compact table")
              )
            case _                                     => "North config!"
          }
        )
      }
      .build
}
