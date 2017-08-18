package edu.msu.mi.syncabm

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter

/**
 * Created by josh on 5/24/17.
 */
class Context {


    AgentFactory agentFactory
    RelationshipFactory relationshipFactory

    List<Agent> agents = []
    Map relationships = [:]

    public Context(AgentFactory aFactory, RelationshipFactory rFactory) {
        this.agentFactory = aFactory
        this.relationshipFactory = rFactory

    }


    public void prepareStudy(int population, List<Map> params, Inspector... inspectors) {
        agents.clear()
        relationships.clear()
        (0..<population).each {
            Agent a = agentFactory.generateAgent(this, params[it % params.size()], it)
            agents << a
            inspectors.each {
                a.addInspector(it)
            }
        }
    }


    public void runStudy(int steps, String output = null, int trial = 0, Closure stoppingCondition = {false}) {

        boolean useHeader = output ? !(new File(output).exists()) : false
        CSVPrinter printer
        relationships = relationshipFactory.wireAgents(agents)
        List<String> globalHeaders = ["trial","agent","step"]
        List<String> headers = agents.sum { it.inspectors?it.inspectors.sum { it.header }:[]  }
        if (output) {
            if (useHeader) {
                printer = new CSVPrinter(new FileWriter(output),
                        CSVFormat.DEFAULT.withHeader((globalHeaders + headers) as String[]))
            } else {
                printer = new CSVPrinter(new FileWriter(output,true),CSVFormat.DEFAULT)
            }

        }

        int step = -1
        while (++step<steps && !stoppingCondition()) {
            if (step == 0) {
                agents.each {
                    it.init()
                }
                agents.each {
                    it.observe(0)
                }

            } else {

                //this should be delegated to the agents themselves

                agents.each {
                    it.step(step)
                }


                agents.each {
                    it.observe(step)
                }

                if (output && !printer) {
                    agents.each {

                    }

                }
                if (output) {
                    agents.each { a ->

                        Map m = a.inspectors.sum { Inspector i ->
                            i.hasData()?i.data:[:]
                        }
                        if (m) {
                            printer.printRecord(
                                    [trial, a.id, step] + headers.collect {
                                        m[it]
                                    }

                            )
                        }
                    }
                }


            }

        }

        printer.flush()
        printer.close()


    }


    public List<Agent> getObserved(Agent a) {
        relationships[a]
    }

    public static void main(String[] args) {



        Context c = new Context(SingleLayerAgent.factory, RelationshipFactory.singleObserverRelationshipFactory)
        c.runStudy(2, 5000, [policy: "OBSERVER", ksize: 4, nsize: 6, vsize: 8], [policy: "GENERATOR", ksize: 4, nsize: 6, vsize: 8])


    }


}
