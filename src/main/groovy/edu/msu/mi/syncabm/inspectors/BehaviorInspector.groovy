package edu.msu.mi.syncabm.inspectors

import edu.msu.mi.syncabm.Agent
import edu.msu.mi.syncabm.Inspector

/**
 * Created by josh on 6/2/17.
 */
class BehaviorInspector extends Inspector.BaseInspector {

    Map data
    int size


    public BehaviorInspector(int size) {
        this.size = size
    }

    def afterAdvance(int step, Agent a) {


        data = [data : (0..<size).collect { a.state.behavior.get(it)?1:0 },
                flag: a.state.metaData.statetype == "INIT"?1:0]


    }

    @Override
    List<String> getHeader() {
        ["data","flag"]
    }

    boolean hasData() {
        true
    }


}
