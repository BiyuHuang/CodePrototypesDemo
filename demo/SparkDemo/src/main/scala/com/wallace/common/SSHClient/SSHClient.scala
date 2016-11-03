package com.wallace.common.SSHClient

import java.io.File

import ch.ethz.ssh2.{Connection, Session}
import com.wallace.common.Using

/**
  * Created by Wallace on 2016/11/3.
  */
class SSHClient(pSSHClient: SSHClientUserInfo) extends Using {
  val clientIP = pSSHClient.sshHost
  val clientUser = pSSHClient.sshUser
  val clientPwd = pSSHClient.sshPwd

  def execute(cmd: String): Boolean = {
    using(getConnection) {
      conn =>
        val session = getSession(conn)
        try {
          session.execCommand(cmd)
          true
        } catch {
          case e: IllegalStateException =>
            log.error("Cannot open session, connection is not authenticated.", e)
            false
          case e: Exception =>
            log.error(s"Execute cmd:[$cmd] Failed.")
            false
        } finally {
          try {
            if (session != null)
              session.close()
            if (conn != null)
              conn.close()
          } catch {
            case e: Exception =>
              log.error(s"Failed to close ssh session.")
          }
        }
    }
  }

  private def getSession(conn: Connection): Session = {
    conn.connect()
    val userHomePath = System.getProperty("user.home")
    val rsaFile = new File(userHomePath + "/.ssh/id_rsa")
    if (rsaFile.exists()) {
      val connResult = conn.authenticateWithPublicKey(clientUser, rsaFile, null)
      if (connResult) {
        log.info("SSH AuthenticateWithPublicKey Successfully.")
      } else {
        log.error("SSH AuthenticateWithPublicKey Failed.")
      }
    } else {
      val connResult = conn.authenticateWithPassword(clientUser, clientPwd)
      if (connResult) {
        log.info("SSH AuthenticateWithPublicKey Successfully.")
      } else {
        log.error("SSH AuthenticateWithPublicKey Failed.")
      }
    }
    val session: Session = conn.openSession()
    session
  }

  private def getConnection: Connection = {
    val conn = new Connection(clientIP)
    conn
  }
}
