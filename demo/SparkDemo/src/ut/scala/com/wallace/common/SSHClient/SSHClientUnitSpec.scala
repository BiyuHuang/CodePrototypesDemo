package com.wallace.common.SSHClient

import com.wallace.UnitSpec

/**
  * Created by Wallace on 2016/11/3.
  */
class SSHClientUnitSpec extends UnitSpec {
  teamID should "Test SSH Client " in {
    val sshParam = SSHClientUserInfo("45.32.83.178", "root", "Dn908018")
    val sshClient = new SSHClient(sshParam)
    val res = sshClient.execute("cd /home/Wallace/ | touch /home/Wallace/test_ssh_exec")
    sshClient.execute("echo \"Test ssh exec\" >> /home/Wallace/test_ssh_exec ")
    sshClient.execute("date -R")
    sshClient.execute("rm -rf /home/Wallace/test_ssh_exec")
    val expect = true
    res shouldBe expect
  }
}
