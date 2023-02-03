package com.wallace.demo.app.utils.zkUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.Participant;

import java.util.concurrent.CountDownLatch;

/**
 * Author: biyu.huang
 * Date: 2023/2/3 11:41
 * Description:
 */
public class LeaderElectionMain {
  private static final CountDownLatch count = new CountDownLatch(50);

  public static void main(String[] args) throws Exception {
    String id = args[0];
    CuratorFramework client = CuratorClient.getCuratorClient();
    LeaderElectionAgent leaderElectionAgent =
        new LeaderElectionAgent(client, CuratorClient.LEADER_PATH, id);
    try {
      leaderElectionAgent.start();
      Thread.sleep(10000);
      while (count.getCount() > 0) {
        Participant leader = leaderElectionAgent.getLeader();
        System.out.println(id + " --> " + leader);
        if (leaderElectionAgent.getLeadership()) {
          System.out.println(id + " is leader");
        } else {
          System.out.println(id + " is not leader");
        }
        count.countDown();
        Thread.sleep(1000);
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      leaderElectionAgent.close();
      client.close();
    }
  }
}
