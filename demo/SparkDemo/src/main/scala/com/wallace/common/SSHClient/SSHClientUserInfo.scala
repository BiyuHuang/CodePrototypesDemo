package com.wallace.common.SSHClient

/**
  * Created by Wallace on 2016/11/3.
  */
case class SSHClientUserInfo(sshHost: String, sshUser: String, sshPwd: String) {
  override def toString: String = s"[SSH PARAM INFO] SSH Host = $sshHost , User = $sshUser , Pwd = $sshPwd"
}
