package edu.msu.mi.syncabm.inspectors

import edu.msu.mi.syncabm.Agent
import edu.msu.mi.syncabm.Inspector

/**
 * Created by josh on 6/2/17.
 */
class StateObservationInspector extends Inspector.BaseInspector {

    Set observedStatespace = [] as Set

    Map data
    List header = ["observed"]
    int interval

    public StateObservationInspector(int interval) {
        this.interval = interval
    }

    @Override
    def afterObservation(int step, Agent a) {
        if (!a.observations.isEmpty()) {
            observedStatespace << a.observations[0].behavior
        }

        if (data!=null) data = null

        if (step % interval == 0) {
            data =[observed: observedStatespace.size() / 2**a.parameters.nsize]

        }
    }


    @Override
    boolean hasData() {
        data
    }
}

