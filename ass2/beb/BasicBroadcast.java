/**
 * This file is part of the ID2203 course assignments kit.
 * 
 * Copyright (C) 2009-2013 KTH Royal Institute of Technology
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.ict.id2203.components.beb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.ict.id2203.pa.broadcast.BebMessage;
import se.kth.ict.id2203.pa.broadcast.Pp2pMessage;
import se.kth.ict.id2203.ports.beb.BebBroadcast;
import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.kth.ict.id2203.ports.beb.BestEffortBroadcast;
import se.kth.ict.id2203.ports.pp2p.PerfectPointToPointLink;
import se.kth.ict.id2203.ports.pp2p.Pp2pSend;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.address.Address;

import java.util.HashSet;

public class BasicBroadcast extends ComponentDefinition {

	private static final Logger logger = LoggerFactory.getLogger(BasicBroadcast.class);

	private Negative<BestEffortBroadcast> beb;
	private Positive<PerfectPointToPointLink> pp2pPos;

	private HashSet<Address> allProcesses;
	private Address thisProcess;


	private Handler<BebBroadcast> handleBroadcast = new Handler<BebBroadcast>() {
		@Override
		public void handle(BebBroadcast event) {
			String message = ((BebMessage)event.getDeliverEvent()).getMessage();
			for(Address process : allProcesses){
				trigger(new Pp2pSend(process, new BebDataMessage(thisProcess, message)), pp2pPos);
			}
		}
	};

	private Handler<BebDataMessage> handleDeliver = new Handler<BebDataMessage>() {
		@Override
		public void handle(BebDataMessage event) {
			String message = event.getMessage();
			Address source = event.getSource();
			trigger(new BebDeliverMessage(source, message), beb);
		}
	};

	public BasicBroadcast(BasicBroadcastInit init) {
		System.out.println("Debug beb 0");
		allProcesses = new HashSet<>(init.getAllAddresses());
		thisProcess = init.getSelfAddress();
		beb = provides(BestEffortBroadcast.class);
		pp2pPos = requires(PerfectPointToPointLink.class);
		System.out.println("Debug beb 1");
		subscribe(handleBroadcast, beb);
		System.out.println("Debug beb 2");
		subscribe(handleDeliver, pp2pPos);
		System.out.println("Debug beb 3");
	}

}
