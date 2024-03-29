package io.github.llfrometa89.infrastructure.controllers

import cats.effect.Sync
import cats.implicits._
import io.github.llfrometa89.application.dto.{LoginDto, RegisterDto}
import io.github.llfrometa89.application.services.UserService
import io.github.llfrometa89.domain.models.User.UserError
import io.github.llfrometa89.infrastructure.controllers.error_handlers.HttpErrorHandler
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

case class UserController[F[_]: Sync: UserService]() extends Http4sDsl[F] {

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {

    case req @ POST -> Root / USERS / LOGIN =>
      for {
        loginData <- req.as[LoginDto]
        session   <- UserService[F].login(loginData)
        resp      <- Ok(session)
      } yield resp
    case req @ POST -> Root / USERS / REGISTER =>
      for {
        registerData <- req.as[RegisterDto]
        result       <- UserService[F].register(registerData)
        resp         <- Ok(result)
      } yield resp
  }

  def routes(implicit H: HttpErrorHandler[F, UserError]): HttpRoutes[F] = H.handle(httpRoutes)
}
