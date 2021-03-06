import scala.quoted._

inline def visitExportsTreeAccumulator[T](inline x: T)(inline f: String => Any): Any = ${ traverseExportsImpl('x, 'f) }
inline def visitExportsTreeMap[T](inline x: T)(inline f: T => Any): Any = ${ visitExportsTreeMapImpl('x, 'f) }
inline def visitExportsExprMap[T](inline x: T)(inline f: T => Any): Any = ${ visitExportsExprMapImpl('x, 'f) }
inline def visitExportsShow[T](inline x: T): Any = ${ visitExportsShowImpl('x) }
inline def visitExportsShowExtract[T](inline x: T): Any = ${ visitExportsShowExtractImpl('x) }
inline def visitExportsSplice(inline l: Logger): Logger = ${ mixinLoggerImpl('l) }
inline def visitExportsSpliceInverse(inline op: Logger => Logger): Logger = ${ mixinLoggerInverseImpl('op) }

private def visitExportsExprMapImpl[T: Type](e: Expr[T], f: Expr[T => Any])(using Quotes): Expr[Any] =
  '{$f(${IdempotentExprMap.transform(e)})}

private def visitExportsTreeMapImpl[T: Type](e: Expr[T], f: Expr[T => Any])(using Quotes): Expr[Any] =
  import quotes.reflect._
  object m extends TreeMap
  '{$f(${m.transformTerm(Term.of(e))(Symbol.spliceOwner).asExprOf})}

private def visitExportsShowImpl[T: Type](e: Expr[T])(using Quotes): Expr[Any] =
  import quotes.reflect._
  '{println(${Expr(Term.of(e).show)})}

private def visitExportsShowExtractImpl[T: Type](e: Expr[T])(using Quotes): Expr[Any] =
  import quotes.reflect._
  '{println(${Expr(Term.of(e).show(using Printer.TreeStructure))})}

private object IdempotentExprMap extends ExprMap {

  def transform[T](e: Expr[T])(using Type[T])(using Quotes): Expr[T] =
    transformChildren(e)

}

private def traverseExportsImpl(e: Expr[Any], f: Expr[String => Any])(using Quotes): Expr[Any] = {
  import quotes.reflect._
  import collection.mutable

  object ExportAccumulator extends TreeAccumulator[mutable.Buffer[String]] {
    def foldTree(x: mutable.Buffer[String], tree: Tree)(owner: Symbol): mutable.Buffer[String] = tree match {
      case tree: Export => foldOverTree(x += s"'{${tree.show}}", tree)(owner)
      case _            => foldOverTree(x, tree)(owner)
    }
  }

  val res =
    ExportAccumulator.foldTree(mutable.Buffer.empty, Term.of(e))(Symbol.spliceOwner).mkString(", ")

  '{ $f(${Expr(res)}) }
}

private def mixinLoggerImpl(l: Expr[Logger])(using Quotes): Expr[Logger] =
  '{ new Logger {
    private val delegate = $l
    export delegate._
   }}

private def mixinLoggerInverseImpl(op: Expr[Logger => Logger])(using Quotes): Expr[Logger] =
  '{ $op(new Logger { def log(a: String): Unit = println(a) }) }
