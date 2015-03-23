package utils

import akka.actor._
import akka.testkit.{ImplicitSender, TestKit}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, Inspectors, Matchers, WordSpecLike}

abstract class BaseAkkaTest extends TestKit(ActorSystem("test-actor-system")) with ImplicitSender

trait BaseTest extends WordSpecLike with Matchers with TypeCheckedTripleEquals with Inspectors with BeforeAndAfterAll

abstract class AkkaTest extends BaseAkkaTest with BaseTest

