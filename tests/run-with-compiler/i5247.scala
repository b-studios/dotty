import scala.quoted._
object Test {
  def main(args: Array[String]): Unit = {
    implicit val toolbox: scala.quoted.Toolbox = scala.quoted.Toolbox.make(getClass.getClassLoader)
    println(run(foo[Object].show.toExpr))
    println(run(bar[Object].show.toExpr))
  }
  def foo[H : Type]: Expr[H] = {
    val t = '[H]
    '{ null.asInstanceOf[$t] }
  }
  def bar[H : Type]: Expr[List[H]] = {
    val t = '[List[H]]
    '{ null.asInstanceOf[$t] }
  }
}
