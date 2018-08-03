package org.onecellboy.akka.cluster;

import akka.actor.AbstractActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.MemberEvent;
import akka.cluster.ClusterEvent.MemberRemoved;
import akka.cluster.ClusterEvent.MemberUp;
import akka.cluster.ClusterEvent.UnreachableMember;
import akka.cluster.protobuf.msg.ClusterMessages;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class SimpleClusterListener extends AbstractActor {
	 LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
	 Cluster cluster = Cluster.get(getContext().getSystem());
	 
	 
	 //subscribe to cluster changes
	  @Override
	  public void preStart() {
	    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), 
	        MemberEvent.class, UnreachableMember.class);
	  }
	  
	  private String str="1";
	  
	  public SimpleClusterListener(String str)
	  {
		  this.str=str;

	  }

	  //re-subscribe when restart
	  @Override
	  public void postStop() {
		  log.info("postStop");
	    cluster.unsubscribe(getSelf());
	  }

	  @Override
	  public Receive createReceive() {
	    return receiveBuilder()
	    		.match(String.class, s->{
	    		
	    			 log.info("message =  {} : {} {}",s,this.getSelf(),str);
	    		})
	      .match(MemberUp.class, mUp -> {
	        log.info("Member is Up: {}", mUp.member());
	      })
	      .match(UnreachableMember.class, mUnreachable -> {
	      	cluster.down(mUnreachable.member().address());
	        log.info("Member detected as unreachable: {}", mUnreachable.member());
	      })
	      .match(MemberRemoved.class, mRemoved -> {
	        log.info("Member is Removed: {}", mRemoved.member());
	      })
	      .match(MemberEvent.class, message -> {
	    	  log.info("Member MemberEvent: {}", message.toString());
	      }).matchAny(o->{
				log.info("Match Any : {}",o.toString());
			})
	      .build();
	  }
}
