package se.kth.ict.id2203.components.beb;

import se.kth.ict.id2203.ports.beb.BebDeliver;
import se.sics.kompics.address.Address;

public class BebDeliverMessage extends BebDeliver {

    private static final long serialVersionUID = 407878878787792579L;
    private String message;
    private Address source;

    public BebDeliverMessage(Address source, String message) {
        super(source);
        this.source = source;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public Address getSource() {
        return source;
    }
}
