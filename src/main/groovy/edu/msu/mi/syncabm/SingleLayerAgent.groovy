package edu.msu.mi.syncabm

/**
 * Created by josh on 5/24/17.
 */
class SingleLayerAgent implements Agent {


    List<SingleAgentState> states = []
    Context context
    Map parameters
    List inspectors=[]

    List connections
    Utils u


    int step
    int id


    List<SingleAgentState> observations = []

    Set<List> allcombinations = [] as Set

    public SingleLayerAgent(Context context, Map params, int id) {
        this.context = context
        this.parameters = params
        this.id = id
        u = Utils.instance(new Random())
        allcombinations.addAll(u.combinations(parameters.ksize, 0..<parameters.nsize))

        createNetwork()
        step = 0
    }

    @Override
    void addInspector(Inspector i) {
       inspectors<<i
    }


    @Override
    public void init() {
        states << new SingleAgentState(u.getRandomBitSet(parameters.nsize), step, "INIT")
    }


    @Override
    public void observe(int step) {
        if (this.step != step) {
            //do nothing
            throw new RuntimeException("Agent out of step - expecting ${this.step}, but received $step")
        }
        inspectors.each {
            it.beforeObservation(step,this)

        }
        if (parameters?.policy == "OBSERVER") {

            Agent source = context.getObserved(this)[0]
            observations << source.getState()

        }
        inspectors.each {
            it.afterObservation(step,this)


        }
    }

    @Override
    public void step(int i) {

        if ((i - step) != 1) {
            //let's kill the system for now
            throw new RuntimeException("Cannot step ${i - step} steps")
        } else {
            inspectors.each { it.beforeAdvance(i,this) }
            learnFromObservations()
            advanceState()
            inspectors.each { it.afterAdvance(i,this) }

        }
    }


    @Override
    public State getState() {
        states.last()

    }

    //learn from one observation right now
    protected learnFromObservations() {

        if (!observations.isEmpty()) {

            if (observations[0].metaData.statetype != "INIT") {
                BitSet obs = observations[0].behavior

                //for now, let 's just find the first one that doesn' t match
                Integer target = u.shuffle((0..<parameters.nsize).toList()).find {
                    state.behavior.get(it) != obs.get(it)
                }

                if (target != null) {

                        update(states[states.size() - 2].behavior, target, obs[target])

                }  else {

                }
            } else {
                states.clear()
            }
        }



        if (parameters.policy == "OBSERVER") {
            states << new SingleAgentState(observations[0].behavior, step, "COPY")
        }

        observations.clear()

    }

    protected int getFxIndex(BitSet state, int idx) {
        int order = 0
        connections[idx].connections.sum { f ->
            (state.get(f) ? 1 : 0) * 2**order++
        }
    }


    private void update(BitSet last, int idx, boolean desired) {
        Map nodefunction = connections.get(idx)
        int fxidx = getFxIndex(last, idx)
        LearningData ld = nodefunction.learn
        ld.update(fxidx, desired)


        if (ld.shouldRewire()) {
            //find the first output val at this node that matches the desired end state
            List candidates = (allcombinations - ld.rejected).toList()
            nodefunction.connections = candidates[u.rand.nextInt(candidates.size())]
            ld.rewire(nodefunction.connections)

        } else {

            nodefunction.fx.set(fxidx, desired)
        }


    }

    private createNetwork() {

        connections = []
        (0..<parameters.nsize).each {
            List incoming = (u.shuffle((0..<parameters.nsize).toList() - it).take(parameters.ksize)).sort()
            connections << [connections: incoming, fx: u.getRandomBitSet(2**parameters.ksize), learn: new LearningData(incoming)]

        }

    }

    private class LearningData {


        Set<List> rejected = [] as Set

        List testing = null

        Boolean[] desired = new Boolean[2**parameters.ksize]

        public LearningData(List elts) {
            this.testing = elts
        }

        public void update(int idx, Boolean value) {
            if (testing != null) {
                if (desired[idx] == null) {
                    desired[idx] = value
                } else if (desired[idx] != value) {
                    rejected << testing
                    testing = null
                }
            }

        }

        public boolean shouldRewire() {
            testing == null
        }

        public void rewire(List elts) {
            testing = elts
            desired = new Boolean[2**parameters.ksize]
        }


    }


    protected advanceState() {

        if (parameters?.policy == "GENERATOR" && states.size() > 1) {

            //check to see if we're in an attractor; if so, re-init
            if (states.subList(states.findLastIndexOf { it.metaData.statetype == "INIT" }, states.size() - 1).find {
                it.behavior == state.behavior
            }) {
                states.clear()
                states << new SingleAgentState(u.getRandomBitSet(parameters.nsize), ++step, "INIT")

                return
            }
        }

        // either we're not in an attractor, or we're not a generator
        def nstate = new SingleAgentState(calculateAdvance(connections, state.behavior), ++step, "GENERATE")

        states << nstate


    }

    private static BitSet calculateAdvance(List<Map> network, BitSet probe) {
        BitSet result = new BitSet()
        network.eachWithIndex { Map obj, int idx ->
            int order = 0
            if (obj.fx.get(obj.connections.sum { f ->
                (probe.get(f) ? 1 : 0) * 2**order++
            })) result.set(idx)
        }
        result
    }


    public static class SingleAgentState implements State {


        BitSet behavior
        Integer step
        Map metaData

        public SingleAgentState(BitSet state, int step, String meta) {
            this.behavior = state
            this.step = step
            this.metaData = [statetype: meta]

        }

        public String toString() {
            "${behavior ?: "{no state}"}, step=$step, ${metaData}"
        }

    }

    public static AgentFactory getFactory() {
        { Context c, Map parameters, int id -> new SingleLayerAgent(c, parameters, id) } as AgentFactory
    }

    public Float compareTo(Agent agent) {
        (0..<2**parameters.nsize).sum {
            calculateAdvance(connections, BitSet.valueOf([it] as long[])) ==
                    calculateAdvance(agent.connections, BitSet.valueOf([it] as long[])) ? 1 : 0
        } / (2**parameters.nsize)
    }

    public String toString() {
        return "SingleLayerAgent.$id : ${state}"
    }



}
