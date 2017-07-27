package com.wallace.common.sshclient

import java.io._
import java.nio.charset.Charset

import ch.ethz.ssh2.{ChannelCondition, Connection, Session, StreamGobbler}
import com.wallace.common.Using

import scala.util.control.Breaks._

/**
  * Created by Wallace on 2016/11/3.
  */
class SshClient(pSSHClient: SshClientUserInfo) extends Using {

  private val clientIP = pSSHClient.sshHost
  private val clientUser = pSSHClient.sshUser
  private val clientPwd = pSSHClient.sshPwd
  private val charset = Charset.defaultCharset.toString

  def executeCmdSet(cmdSet: String*): Unit = {
    using(getConnection) {
      conn =>
        val session = getSession(conn)
        val stdout: StreamGobbler = new StreamGobbler(session.getStdout)
        val stdoutReader: BufferedReader = new BufferedReader(new InputStreamReader(stdout))

        try {
          session.requestPTY("bash")
          session.startShell()
          val out = new PrintWriter(session.getStdin)
          for (cmd <- cmdSet) {
            out.println(cmd)
            out.flush()
            session.waitForCondition(ChannelCondition.CLOSED | ChannelCondition.EOF | ChannelCondition.EXIT_STATUS, 1000)
            breakable {
              while (true) {
                val line: String = stdoutReader.readLine()
                if (line.nonEmpty) {
                  println(s"#### $line")
                }
                else {
                  break
                }

              }
            }
          }
          out.close()

          log.debug(s"[SSHClient] ${session.getExitStatus}")
        }
        catch {
          case e: IllegalStateException =>
            log.error("[SSHClient] Cannot open session, connection is not authenticated.", e)
          case e: Exception =>
            log.error(s"[SSHClient] Failed to execute command.Exception Message: ${e.getMessage}")
        }
        finally {
          try {
            if (session != null) {
              session.close()
            }
            if (conn != null)
              conn.close()
          } catch {
            case e: Exception =>
              log.error(s"[SSHClient] Failed to close ssh session.Exception Message: ${e.getMessage}")
          }
        }
    }
  }

  def execute(cmd: String): Boolean = {
    using(getConnection) {
      conn =>
        val session = getSession(conn)
        try {
          session.execCommand(cmd)
          val stdOut: StreamGobbler = new StreamGobbler(session.getStdout)
          val outStr: String = processStream(stdOut, charset)
          log.debug(s"[SSHClient] execute cmd: $cmd, return result: $outStr.")
          log.debug(s"${session.getExitStatus}")
          true
        } catch {
          case e: IllegalStateException =>
            log.error("[SSHClient] Cannot open session, connection is not authenticated.", e)
            false
          case e: Exception =>
            log.error(s"[SSHClient] Failed to execute command: $cmd.Exception Message: ${e.getMessage}")
            false
        } finally {
          try {
            if (session != null)
              session.close()
            if (conn != null)
              conn.close()
          } catch {
            case e: Exception =>
              log.error(s"[SSHClient] Failed to close ssh session.Exception Message: ${e.getMessage}")
          }
        }
    }
  }

  private def getSession(conn: Connection): Session = {
    conn.connect()
    val os = System.getProperty("os.name")
    val userHomePath = System.getProperty("user.home")
    val rsaFile = new File(userHomePath + "/.ssh/id_rsa")
    if (rsaFile.exists() && !os.toLowerCase.contains("windows")) {
      val connResult = conn.authenticateWithPublicKey(clientUser, rsaFile, null)
      if (connResult) {
        log.info("[SSHClient] SSH AuthenticateWithPublicKey Successfully.")
      } else {
        log.error("[SSHClient] SSH AuthenticateWithPublicKey Failed.")
      }
    } else {
      val connResult = conn.authenticateWithPassword(clientUser, clientPwd)
      if (connResult) {
        log.info("[SSHClient] SSH AuthenticateWithPublicKey Successfully.")
      } else {
        log.error("[SSHClient] SSH AuthenticateWithPublicKey Failed.")
      }
    }
    val session: Session = conn.openSession()
    session
  }

  private def getConnection: Connection = {
    val conn = new Connection(clientIP)
    conn
  }

  @throws[IOException]
  private def processStream(in: InputStream, charset: String): String = {
    val buf = new Array[Byte](1024)
    val sb = new StringBuilder
    while (in.read(buf) != -1) {
      sb.append(new String(buf, charset))
    }
    sb.toString
  }

}
