package com.wallace.common.SSHClient

import com.wallace.common.LogSupport
import org.scalatest.{FlatSpec, ShouldMatchers}

/**
  * Created by Wallace on 2016/11/3.
  */
class SSHClientUnitSpec extends FlatSpec with ShouldMatchers with LogSupport {
  "Team Legend" should "Test SSH Client " in {
    val sshParam = SSHClientUserInfo("45.76.65.120", "root", "Dn908018")
    val sshClient = new SSHClient(sshParam)
    val res = sshClient.execute("cd /home/Wallace/ | touch /home/Wallace/test_ssh_exec")

    sshClient.execute("echo \"Test ssh exec\" >> /home/Wallace/test_ssh_exec ")
    val expect = true
    res shouldBe expect
  }
}
