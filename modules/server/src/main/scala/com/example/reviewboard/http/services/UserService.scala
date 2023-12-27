package com.example.reviewboard.http.services

import zio.*
import com.example.reviewboard.http.domain.data.*
import com.example.reviewboard.http.repositories.UserRepository

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import Hasher.*

trait UserService:
  def registerUser(email: String, pwd: String): Task[User]
  def verifyPassword(email: String, password: String): Task[Boolean]
  def generateToken(email: String, password: String): Task[Option[UserToken]]

class UserServiceLive private (jwtService: JWTService, repo: UserRepository) extends UserService:
  override def registerUser(email: String, pwd: String): Task[User] =
    repo.create(User(id = -1L, email = email, hashedPwd = generateHash(pwd)))
  override def verifyPassword(email: String, password: String): Task[Boolean] = for {
    user <- repo.getByEmail(email).someOrFail(new Exception("User not found"))
    result <- ZIO.attempt(validateHashedPwd(password, user.hashedPwd))
  } yield result
  override def generateToken(email: String, password: String): Task[Option[UserToken]] = for {
    user     <- repo.getByEmail(email).someOrFail(new Exception("User not found"))
    verified <- ZIO.attempt(validateHashedPwd(password, user.hashedPwd))
    token    <- jwtService.createToken(user).when(verified)
  } yield token

object UserServiceLive:
  val layer = for {
    jwtService <- ZIO.service[JWTService]
    repo       <- ZIO.service[UserRepository]
  } yield new UserServiceLive(jwtService, repo)

object Hasher:
  private val PBKDF2_ITERATIONS = 1000
  private val SALT_BYTES = 24
  private val PBKDF2_ALGO: String = "PBKDF2WithHmacSHA256"
  private val HASH_BYTE_SIZE = 24
  private val skf = SecretKeyFactory.getInstance(PBKDF2_ALGO)

  private def pbkdf2(password: Array[Char], salt: Array[Byte], iterations: Int, nBytes: Int): Array[Byte] = {
    val spec = new PBEKeySpec(password, salt, iterations, nBytes * 8)
    skf.generateSecret(spec).getEncoded()
  }
  private def toHex(array: Array[Byte]): String = array.map("%02x".format(_)).mkString
  private def fromHex(hex: String): Array[Byte] = hex.sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  private def compareBytes(a: Array[Byte], b: Array[Byte]): Boolean = {
    val rng = 0 until Math.min(a.length, b.length)
    val diff = rng.foldLeft(a.length ^ b.length) { (acc, i) => acc | (a(i) ^ b(i)) }
    diff == 0
  }

  def generateHash(message: String): String = {
    val rng = new SecureRandom()
    val salt = Array.ofDim[Byte](SALT_BYTES)
    rng.nextBytes(salt)
    val hashedBytes = pbkdf2(message.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE)
    s"$PBKDF2_ITERATIONS:${toHex(salt)}:${toHex(hashedBytes)}"
 }
  def validateHashedPwd(pwd: String, hashedPwd: String): Boolean =
    val hashedPwdArr = hashedPwd.split(":")
    val iterations = hashedPwdArr(0).toInt
    val salt = fromHex(hashedPwdArr(1))
    val validHash = fromHex(hashedPwdArr(2))
    val testHash = pbkdf2(pwd.toCharArray(), salt, iterations, HASH_BYTE_SIZE)
    compareBytes(testHash, validHash)

object HasherDemo extends App:
    val hashed = Hasher.generateHash("123456")
    println(validateHashedPwd("123456", hashed))