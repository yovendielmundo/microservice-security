package io.github.llfrometa89.infrastructure.controllers.error_handlers

import cats.ApplicativeError
import cats.data.{Kleisli, OptionT}
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

abstract class RoutesHttpErrorHandler[F[_], E <: Throwable] extends HttpErrorHandler[F, E] with Http4sDsl[F] {
  def A: ApplicativeError[F, E]
  def handler: E => F[Response[F]]
  def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
    Kleisli { req =>
      OptionT {
        A.handleErrorWith(routes.run(req).value)(e => A.map(handler(e))(Option(_)))
      }
    }
}

object HttpErrorHandler {
  @inline final def apply[F[_], E <: Throwable](implicit ev: HttpErrorHandler[F, E]): HttpErrorHandler[F, E] = ev
}
