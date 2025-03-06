package com.wallace.demo.app.utils.zkutils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Author: biyu.huang
 * Date: 2023/2/2 18:53
 * Description:
 */
public class LeaderElectionAgent implements LeaderLatchListener {
  private static final Logger logger = LoggerFactory.getLogger(LeaderElectionAgent.class);
  private final AtomicBoolean LEADERSHIP_FLAG;

  private final LeaderLatch leaderLatch;

  public LeaderElectionAgent(CuratorFramework client,
                             String latchPath,
                             String id) throws Exception {
    this.leaderLatch = new LeaderLatch(client, latchPath, id);
    this.LEADERSHIP_FLAG = new AtomicBoolean(false);
  }

  public boolean getLeadership() {
    return this.LEADERSHIP_FLAG.get();
  }

  @Override
  public void isLeader() {
    logger.info("master -> set LEADER_FLAG to ture");
    this.LEADERSHIP_FLAG.set(true);
  }

  @Override
  public void notLeader() {
    logger.info("slave -> set LEADER_FLAG to false");
    this.LEADERSHIP_FLAG.set(false);
  }

  public Participant getLeader() throws Exception {
    return this.leaderLatch.getLeader();
  }

  public void start() throws Exception {
    this.leaderLatch.addListener(this);
    this.leaderLatch.start();
  }

  public void close() throws IOException {
    this.leaderLatch.close();
  }
}
