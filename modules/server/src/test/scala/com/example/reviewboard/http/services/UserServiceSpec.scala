package com.example.reviewboard.http.services

import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.http.repositories.UserRepository
import com.example.reviewboard.http.services.UserServiceLive

import zio.test.*
import zio.*

object UserServiceSpec extends ZIOSpecDefault:
  val bob = User(1, "bob@g.com", "1000:cbd1430cd9217df389bb1531b34f5146d644ea1004e9846a:dd105873e7cb52efac54c227bd1eaf32717b217f4b5065f4")
  val stubRepoLayer = ZLayer.succeed {
    new UserRepository {
      var db: Map[Long, User] = Map(1L -> bob)

      override def create(user: User): Task[User] = ZIO.succeed {
        db = db + (user.id -> user)
        user
      }
      override def getById(id: Long): Task[Option[User]] =
        ZIO.succeed(db.get(id))
      override def getByEmail(email: String): Task[Option[User]] =
        ZIO.succeed(db.values.find(_.email == email))
      override def update(id: Long, op: User => User): Task[User] =
        ZIO.attempt {
          val newUser = op(db(id))
          db = db + (id -> newUser)
          newUser
        }
      override def delete(id: Long): Task[User] = ZIO.attempt {
        val user = db(id)
        db = db - id
        user
      }
    }
  }

  val stubJwtLayer = ZLayer.succeed {
    new JWTService {
      override def createToken(user: User): Task[UserToken] =
        ZIO.succeed(UserToken(user.email, "BIG ACCESS", Long.MaxValue))
      override def verifyToken(token: String): Task[UserId] =
        ZIO.succeed(UserId(bob.id, bob.email))
    }
  }

  override def spec: Spec[TestEnvironment & Scope, Any] = 
    suite("UserServiceSpec")(
      test("create and validate user") {
        for {
          service  <- ZIO.service[UserService]
          user     <- service.registerUser(bob.email, "123456")
          verified <- service.verifyPassword(bob.email, "123456")
        } yield assertTrue(verified && user.email == bob.email)
      },
      test("Invalidate incorrect credentials") {
        for {
          service  <- ZIO.service[UserService]
          user     <- service.registerUser(bob.email, "123456")
          verified <- service.verifyPassword(bob.email, "222")
        } yield assertTrue(!verified)
      },
      test("invalidate non existing user") {
        for {
          service  <- ZIO.service[UserService]
          verified <- service.verifyPassword("non-existing", "222")
        } yield assertTrue(!verified)
      },
      test("update password") {
        for {
          service <- ZIO.service[UserService]
          user    <- service.registerUser(bob.email, "123456")
          _       <- service.updatePassword(bob.email, "123456", "new-password")
          verified <- service.verifyPassword(bob.email, "new-password")
        } yield assertTrue(verified)
      },
      test("delete user") {
        for {
          service <- ZIO.service[UserService]
          _       <- service.registerUser(bob.email, "123456")
          _       <- service.deleteUser(bob.email, "123456")
        } yield assertTrue(true)
      },
        test("delete non existing user") {
          for {
            service <- ZIO.service[UserService]
            err       <- service.deleteUser("non-existing", "123456").flip
          } yield assertTrue(err.isInstanceOf[RuntimeException])
        }
    ).provide(
      UserServiceLive.layer,
      stubRepoLayer,
      stubJwtLayer
    )
