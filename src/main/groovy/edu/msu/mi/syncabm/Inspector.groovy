package edu.msu.mi.syncabm

/**
 * Created by josh on 5/30/17.
 */
interface Inspector {




    def beforeObservation(int step, Agent a)
    def afterObservation(int step, Agent a)

    def beforeAdvance(int step, Agent a)
    def afterAdvance(int step, Agent a)

    List<String> getHeader()
    boolean hasData()
    Map getData()

    public static abstract class BaseInspector implements Inspector {



        @Override
        def beforeObservation(int step, Agent a) {
            return null
        }

        @Override
        def afterObservation(int step, Agent a) {
            return null
        }

        @Override
        def beforeAdvance(int step, Agent a) {
            return null
        }

        @Override
        def afterAdvance(int step, Agent a) {
            return null
        }


    }

}