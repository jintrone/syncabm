package edu.msu.mi.syncabm

/**
 * Created by josh on 5/24/17.
 */
class Utils {


    Random rand

    private static Utils instance

    public Utils(Random rand) {
        this.rand = rand
    }

    public static Utils instance(Random rand) {
        if (instance == null) {
            instance = new Utils(rand)
        }

        instance

    }

    public static Utils instance() {
        instance
    }

    public List shuffle(List l) {
        List l2 = l.clone()
        Collections.shuffle(l2, rand)
        l2
    }

    public BitSet getRandomBitSet(Long width) {
        long val = 2l**width
        BitSet.valueOf([rand.nextDouble()*val] as long[])

    }

    public findExhaustive(List source, List pattern, int offset = 0) {
        List result = []
        if (source.size() > pattern.size()) {
            int p = 0
            source.eachWithIndex { ent, i ->
                List partial = []
                if (source[i] == pattern[0]) {
                    partial << (i + offset)
                    if (pattern.size() == 1) {
                        result << partial
                    } else {
                        List a = findExhaustive(source.drop(i + 1) as List, pattern.drop(1) as List, offset + i + 1)
                        if (a) {
                            a.each {
                                result << (partial + it)
                            }

                        }
                    }

                }
            }
        } else if (source.size() == pattern.size() && source == pattern) {
            return [offset]
        }
        return result
    }


    public combinations(int m, List list) {
        def n = list.size()
        m == 0 ?
                [[]] :
                (0..(n - m)).inject([]) { newlist, k ->
                    def sublist = (k + 1 == n) ? [] : list[(k + 1)..<n]
                    newlist += combinations(m - 1, sublist).collect { [list[k]] + it }
                }
    }


    public static void main(String[] args) {
        Utils u = Utils.instance(new Random())
        println u.combinations(4,0..<10)
    }

}
