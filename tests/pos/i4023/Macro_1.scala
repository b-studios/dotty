import scala.quoted._
object Macro {
  rewrite def ff[T: Type](x: T): T = ~impl('(x))
  def impl[T](x: Expr[T]): Expr[T] = x
}
