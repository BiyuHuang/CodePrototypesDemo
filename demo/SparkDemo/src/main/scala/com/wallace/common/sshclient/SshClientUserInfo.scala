package com.wallace.common.sshclient

/**
  * Created by Wallace on 2016/11/3.
  */
case class SshClientUserInfo(sshHost: String, sshUser: String, sshPwd: String) {
  override def toString: String = s"[SSH PARAM INFO] SSH Host = $sshHost , User = $sshUser , Pwd = $sshPwd"
}
