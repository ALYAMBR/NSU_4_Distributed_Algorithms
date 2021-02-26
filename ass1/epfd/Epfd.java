/*----------------------------------------------------------
* Made by Nikita Radeev.
* Numbers in comments equal numbers of lines in pseudo-code.
----------------------------------------------------------*/

package se.kth.ict.id2203.components.epfd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.pa.epfd.Pp2pMessage;
import se.kth.ict.id2203.ports.epfd.EventuallyPerfectFailureDetector;
import se.kth.ict.id2203.ports.epfd.Restore;
import se.kth.ict.id2203.ports.epfd.Suspect;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.*;
import se.sics.kompics.address.Address;
import se.sics.kompics.timer.ScheduleTimeout;
import se.sics.kompics.timer.Timer;

import java.util.HashSet;

public class Epfd extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(Epfd.class);

	private Negative<EventuallyPerfectFailureDetector> epfd;
	private Positive<PerfectPointToPointLink> pp2pPos;

	private HashSet<Address> alive;
	private HashSet<Address> suspected;
	private HashSet<Address> allProcesses;

	private Address selfAddress;

	private Positive<Timer> timer = requires(Timer.class);
	private long delay;
	private final long deltaDelay;
	private int seqnum = 0;

	private Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>(){
		@Override
		public void handle(CheckTimeout checkTimeout) {
			HashSet<Address> intersection = new HashSet<>(alive); // 8
			intersection.retainAll(suspected); // 8
			if(!intersection.isEmpty()){ // 8
				delay += deltaDelay; // 9
				logger.info("  delay is {}", delay);
			}
			seqnum++; // 10
			logger.info("\t Seqnum is " + seqnum + " Alive " + alive.toString() + "  Suspected " + suspected.toString());
			for(Address process : allProcesses) { // 11
				if(!alive.contains(process) && !suspected.contains(process)){ // 12
					suspected.add(process); // 13
					trigger(new Suspect(process), epfd); // 14
				}
				else if(alive.contains(process) && suspected.contains(process)){ // 15
					suspected.remove(process); // 16
					trigger(new Restore(process), epfd); // 17
				}
				trigger(new Pp2pSend(process, new HeartbeatRequestMessage(selfAddress, seqnum)), pp2pPos); // 18
			}
			alive.clear(); //19
			ScheduleTimeout st = new ScheduleTimeout(delay); // 20
			st.setTimeoutEvent(new CheckTimeout(st)); // 20
			trigger(st, timer); // 20
		}
	};

	private Handler<HeartbeatRequestMessage> handleRequest = new Handler<HeartbeatRequestMessage>(){
		@Override
		public void handle(HeartbeatRequestMessage heartbeatRequestMessage) {
			trigger(new Pp2pSend(heartbeatRequestMessage.getSource(),
					new HeartbeatReplyMessage(selfAddress, seqnum)), pp2pPos); // 22
		}
	};

	private Handler<HeartbeatReplyMessage> handleReply = new Handler<HeartbeatReplyMessage>(){
		@Override
		public void handle(HeartbeatReplyMessage heartbeatReplyMessage) {
			Address process = heartbeatReplyMessage.getSource(); // 24
			long sn = heartbeatReplyMessage.getSeqnum(); // 24
			if(sn == seqnum || suspected.contains(process)){ // 24
				alive.add(process); // 25
			}
		}
	};

	private Handler<Start> handleStart = new Handler<Start>() {
		@Override
		public void handle(Start start) {
			ScheduleTimeout st = new ScheduleTimeout(delay); // 6
			st.setTimeoutEvent(new CheckTimeout(st)); // 6
			trigger(st, timer); // 6
		}
	};

	public Epfd(EpfdInit init) { // 1
		seqnum = 0; // 2
		alive = new HashSet<>(init.getAllAddresses()); // 3
		allProcesses = new HashSet<>(init.getAllAddresses()); // П
		selfAddress = init.getSelfAddress();
		suspected = new HashSet<>(); // 4
		delay = init.getInitialDelay(); // 5
		deltaDelay = init.getDeltaDelay();
		epfd = provides(EventuallyPerfectFailureDetector.class);
 		pp2pPos = requires(PerfectPointToPointLink.class);
		subscribe(handleStart, control);
		subscribe(handleCheck, timer); // 7
		subscribe(handleRequest, pp2pPos); // 21
		subscribe(handleReply, pp2pPos); // 23

	}
}
