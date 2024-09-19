
import org.scalatest._

/**
  * Created by Wallace on 2016/11/6.
  */
trait AccSpec extends FeatureSpec with GivenWhenThen with ShouldMatchers with BeforeAndAfterAll {

}
object SlowAT extends Tag("Test Demo")
