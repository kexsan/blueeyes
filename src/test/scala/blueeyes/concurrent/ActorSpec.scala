package blueeyes.concurrent

import org.spex.Specification


class ActorSpec extends Specification with ActorExecutionStrategySequential {

  private val actorImplementation = new ActorImplementationSequential{}
  "Actor" should {
    "process message" in {
      val actor = actorImplementation[String, String] {
        case message: String => message + "_done"
      }

     actor("foo").value must eventually (beSome("foo_done"))
    }
  }

  "Actor of future" should {
    "flatten implicitly" in {
      val actor1 = actorImplementation[String, String] {
        case message: String => message + "_done"
      }

      val actor2: Actor[String, String] = Actor[String, Future[String]] {
        case message: String  => actor1 ! message
      }

      (actor2 ! "foo").value must eventually (beSome("foo_done"))
    }
  }

  "Actor examples" should {
    "compile" in {
      // Easy actor definition:
      val squarePositiveA = actorImplementation[Int, Int] {
        case x: Int if (x > 0) => x * x
      }

      val squareNegativeA = Actor[Int, Int] {
        case x: Int if (x < 0) => x * x
      }

      // Constant actors:
      val zeroA = Actor.constant[Int, Int](0)

      // Actor composition based on partial functions:
      val squareAnythingA = squarePositiveA.orElse(squareNegativeA).orElse(zeroA)

      // Actor composition based on arrows:
      val pow4A = squareAnythingA >>> squareAnythingA

      // Actor response mapping:
      val pow4AsStringA = pow4A.map(_.toString)

      pow4AsStringA(0).value must eventually (beSome("0"))
      pow4AsStringA(1).value must eventually (beSome("1"))
      pow4AsStringA(2).value must eventually (beSome("16"))
    }
  }

  "Actor to Actor" in{
    "deliver future in actor thread" in{
      var deliveried = false
      val actor1 = Actor[String, Unit]{
        case message: String => {
          val thread  = Thread.currentThread
           val actor2 = Actor[String, String]{
            case message: String => "actor2"
           }
          val future = actor2("bar")

          future.deliverTo{v =>
            deliveried = thread == Thread.currentThread
          }
        }
      }
      actor1("test")

      deliveried must eventually (be(true))
    }
  }
}