package blueeyes.core.service.engines.netty

import org.jboss.netty.handler.codec.http.HttpRequestDecoder
import util.matching.Regex

private[engines] class FullURIHttpRequestDecoder(protocol: String, host: String, port: Int, chunkSize: Int) extends HttpRequestDecoder(4096, 8192, chunkSize){
  private val baseUri = """%s://%s:%d""".format(protocol, host, port)
  private val fullUriRegexp = new Regex("""(https|http)://.+/(:\d+/)?.+""")
  override def createMessage(initialLine: Array[String]) = {
    val path = initialLine(1)
    if (!fullUriRegexp.pattern.matcher(path).matches) initialLine(1) = baseUri + (if (path.startsWith("/")) path else "/" + path)
    super.createMessage(initialLine)
  }
}