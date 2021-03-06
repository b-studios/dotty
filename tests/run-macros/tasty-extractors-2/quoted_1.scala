import scala.quoted._

object Macros {

  implicit inline def printTree[T](inline x: T): Unit =
    ${ impl('x) }

  def impl[T](x: Expr[T])(using q: Quotes) : Expr[Unit] = {
    import q.reflect._

    val tree = Term.of(x)

    val treeStr = Expr(tree.show(using Printer.TreeStructure))
    val treeTpeStr = Expr(tree.tpe.show(using Printer.TypeReprStructure))

    '{
      println(${treeStr})
      println(${treeTpeStr})
      println()
    }
  }
}
