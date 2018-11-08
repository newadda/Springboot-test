package org.onecellboy.akka.distributeddata;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.cluster.Cluster;
import akka.cluster.ddata.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.Option;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static java.time.Duration.ofSeconds;
import static scala.concurrent.duration.Duration.create;

public class DdataActor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static class ExampleState implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private  Integer count = new Integer(0);
    }
    private ExampleState state = new ExampleState();
    Cancellable scheduleCancellable;


    private Cluster node = Cluster.get(getContext().getSystem());

    private final String key = "singleton-data";
    private final Key<LWWMap<String, Object>> dataKey = LWWMapKey.create(key);

    private final ActorRef replicator =
            DistributedData.get(getContext().getSystem()).replicator();



    @Override
    public void postStop() throws Exception {
        log.info("postStop()");
        super.postStop();

    }



    @Override
    public void preStart() throws Exception {
        log.info("preStart()");

        super.preStart();

        /**
         * �л� �����Ϳ� ���� ���� ���� , �ش� key �� ���� �۾��� ���� �̺�Ʈ�� ���� �� �ִ�.
         */
        Replicator.Subscribe<LWWMap<String, Object>> subscribe = new Replicator.Subscribe<>(dataKey, getSelf());
        replicator.tell(subscribe, ActorRef.noSender());
    }

    public void schedule()
    {
        scheduleCancellable = getContext().getSystem().scheduler().scheduleOnce(create(3, TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        getSelf().tell("timer", ActorRef.noSender());
                    }
                }, getContext().getSystem().dispatcher());
    }


    /**
     *
     * �� �κ��� �޼����� �޴� �κ��̴�.
     * */
    @Override
    public Receive createReceive() {
        // TODO Auto-generated method stub

        return receiveBuilder()
                .match(String.class,  s->{
                    log.info("Received String message: {}", s);
                    // log.info("====== ������(����) ���� : {}", state.count );

                    // saveSnapshot(state);
                    // state.count = state.count+1;
                    //  saveData( key,state.count );
                    // schedule();
                    get();
                }  )
                /* �л� ������ ��� ������*/
                .match(Replicator.GetSuccess.class, g -> receiveGetSuccess((Replicator.GetSuccess<LWWMap<String, Object>>) g))
                /* �л� �������� �ش� Ű�� �������� ���� ��*/
                .match(Replicator.NotFound.class, a -> {
                    saveData(key,new Integer(0));
                    schedule();
                })
                .matchAny(o->{
                    log.info("Received unKnown message : {}",o);
                    //  schedule();
                })
                .build();

    }

    /**
     * ������ ��� ��û
     */
    private void get() {
        // final Replicator.ReadConsistency readAll = new Replicator.ReadAll(ofSeconds(5));
        //   final Replicator.ReadConsistency readFrom = new Replicator.ReadFrom(1, ofSeconds(3));
        Replicator.Get<LWWMap<String, Object>> get = new Replicator.Get<>(dataKey, Replicator.readLocal());
        replicator.tell(get, self());
    }

    /**
     * ������ ��� ��û�� ���� ����, ������ request() �� �ʱ⿡ ������ ��� ��û���� ���� �������̴�.
     * request() �����ʹ� ������ �޾��� �� � ��û�� ���� ���������� �����ϱ� ���� ����Ѵ�.
     * @param g
     */
    private void receiveGetSuccess(Replicator.GetSuccess<LWWMap<String, Object>> g) {
        Option<Object> valueOption = g.dataValue().get("data");
        Optional<Object> valueOptional = Optional.ofNullable(valueOption.isDefined() ? valueOption.get() : new Integer(0));
        state.count=(Integer)valueOption.get();
        log.info("====== ������(����) ���� : {}", state.count );
        saveData(key,state.count+1);
        schedule();
    }

    /**
     * �л� ������ ����ҿ� ������ �����ϱ�
     * @param key
     * @param count
     */
    public void saveData(String key,Integer count)
    {
        final Replicator.WriteConsistency writeAll = new Replicator.WriteAll(ofSeconds(5));
        Replicator.Update<LWWMap<String, Object>> update = new Replicator.Update<>( LWWMapKey.create(key), LWWMap.create(),writeAll,
                curr -> curr.put(node,"data",count));


        replicator.tell(update, self());
    }


}
