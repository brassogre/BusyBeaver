/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package busybeaver;

import java.util.*;
import java.util.concurrent.*;

/**
 *
 * @author dkutach
 */
public class GraphList implements Runnable {

    final protected int size;
    protected volatile int maxrunningtime;
    protected volatile int minmaxrunningtime;
    final protected int targetsize;
    final protected int maxregisters;
    final private GraphList ancestor;
    protected boolean completedcalculation;
    public Set<Long> codes;
    private LinkedBlockingDeque<Graph> graphs;
    private LinkedList<Graph> safegraphs;
    public Set<String> winners;
    private volatile long counter;

    protected final Graph endmarker = new Graph((byte)1);

    public GraphList()
    {
        size = 1;
        graphs = new LinkedBlockingDeque<Graph>(5);
        winners = new HashSet<String>();
        codes = new HashSet<Long>();
        minmaxrunningtime = 1;
        maxrunningtime = 1;
        targetsize = 1;
        ancestor = null;
        counter = 0;
        completedcalculation = false;
        maxregisters = 1;
        safegraphs = new LinkedList<Graph>();

        try
        {
            for (byte n = 1; n <= 3; ++n)
            {
                Graph g = new Graph(n);
                g.setCode();
                if (n > 1)
                    winners.add(g.getLabel());
                else
                    graphs.putLast(g);
            }
            graphs.putLast(endmarker);
        }
        catch (InterruptedException e)
        {
            return;
        }
        completedcalculation = true;
    }
    public GraphList(GraphList gl, int t)
    {
        ancestor = gl;
        size = gl.size + 1;
        maxrunningtime = 1;
        minmaxrunningtime = 1;
        if (size > 1)
            minmaxrunningtime = 3;
        if (size > 2)
            minmaxrunningtime = 5;
        if (size > 3)
            minmaxrunningtime = 10;
        if (size > 4)
            minmaxrunningtime = 16;
        if (size > 5)
            minmaxrunningtime = 27;
        targetsize = t;
        maxregisters = t/2;
        completedcalculation = false;
        graphs = new LinkedBlockingDeque<Graph>(500000);
        safegraphs = new LinkedList<Graph>();
        winners = new HashSet<String>();
        //labels = new HashSet<String>();
        codes = new HashSet<Long>();
        counter = 0;
    }
    public GraphList(int s, int r)
    {
        size = s;
        targetsize = s;
        maxregisters = r;
        graphs = new LinkedBlockingDeque<Graph>(10000);
        safegraphs = new LinkedList<Graph>();
        winners = new HashSet<String>();
        codes = new HashSet<Long>();
        minmaxrunningtime = 1;
        maxrunningtime = 0;
        ancestor = null;
        counter = 0;
        completedcalculation = false;
    }
    protected Graph getGraph()
    {
        if (graphs.isEmpty())
            return null;

        Graph g = null;
        try
        {
            g = graphs.takeFirst();
        }
        catch (InterruptedException e)
        {
            System.err.println("The program failed due to an internal error.");
        }
        return g;
    }
    public void printself()
    {
        System.out.println("Here are all the graphs with " + size +" nodes.");
        for (Graph g : safegraphs)
        {
            g.printself();
        }
    }
    public boolean completed()
    {
        return completedcalculation;
    }
    public long getCounter()
    {
        return counter;
    }
    public int runningtime()
    {
        if (!completedcalculation)
            return 0;

        return maxrunningtime;
    }
    public void run() {
        boolean bFinished = false;
        do
        {
            try
            {
                bFinished = grow();
                if (!bFinished)
                    Thread.sleep(1);
            }
            catch (CloneNotSupportedException e)
            {
                System.err.println("The program failed due to an internal error.");
                bFinished = true;
            }
            catch (InterruptedException e)
            {
                System.err.println("The program failed due to an internal error.");
                bFinished = true;
            }
        } while (!bFinished);
        if (bFinished)
            System.out.println("Completed calculation of r(" + size + ").");
    }

    public boolean grow() throws CloneNotSupportedException
    {
        if (!completedcalculation)
        {
            for (byte r = 1; r <= maxregisters; ++r)
            {
                byte nIncrements = (byte)((byte)targetsize - r);
                byte nDecrements = r;

                LinkedList<LinkedList<Byte>> combos = new LinkedList<LinkedList<Byte>>();
                distributions(nDecrements, nIncrements, combos);

        /* This code tests the function GraphList.distributions(...)
        LinkedList<LinkedList> test = new LinkedList<LinkedList>();
        GraphList.distributions(6, 4, test);
        for (Iterator iter = combos.iterator(); iter.hasNext(); )
        {
            LinkedList list2 = (LinkedList)iter.next();
            System.out.print("{");
            for (Iterator iter2 = list2.iterator(); iter2.hasNext(); )
            {
                byte i = (Byte)(iter2.next());
                System.out.print(i);
                if (iter2.hasNext())
                    System.out.print(",");
            }
            System.out.print("}");
        }
        System.out.println();*/


                for (Iterator<LinkedList<Byte>> iter = combos.iterator(); iter.hasNext(); )
                {
                    LinkedList<Byte> list = iter.next();
                    Graph g = new Graph(list);
                    safegraphs.add(g);
                }

                boolean bWorthTrying;
                for (Graph g : safegraphs)
                {
                    ++counter;
                    bWorthTrying = g.initialize();
                    assert(bWorthTrying);
                    if (bWorthTrying)
                    {
                        int runningtime = g.run();
                        if (runningtime > maxrunningtime)
                        {
                            winners.clear();
                        }
                        if (runningtime >= maxrunningtime)
                        {
                            maxrunningtime = runningtime;
                            winners.add(g.getUnalteredLabel());
                        }
                    }
                }
                safegraphs.clear();
            }
            completedcalculation = true;
        }
        return completedcalculation;
    }

    public boolean fullgrow() throws CloneNotSupportedException
    {
        if (!completedcalculation)
        {
            assert(ancestor != null);
            Graph g = ancestor.getGraph();

            if (g == null)
                return false;

            if (g == ancestor.endmarker)
            {
                try
                {
                    graphs.putLast(endmarker);
                }
                catch (InterruptedException e)
                {
                    return false;
                }
                minmaxrunningtime = maxrunningtime;
                completedcalculation = true;
                codes.clear();
                return true;
            }

            if (g != ancestor.endmarker)
            {
                LinkedList<LinkedList<Byte>> exits = new LinkedList<LinkedList<Byte>>();
                g.exitarrows(exits);
                int numexits = exits.size();
                for (Iterator<LinkedList<Byte>> iter2 = exits.iterator(); iter2.hasNext(); )
                {
                    LinkedList<Byte> exit = iter2.next();
                    LinkedList<LinkedList<LinkedList<Byte>>> extrajoiners = new LinkedList<LinkedList<LinkedList<Byte>>>();
                    if (numexits > 1)
                    {
                    	@SuppressWarnings("unchecked")
						LinkedList<LinkedList<Byte>> alljoiners = (LinkedList<LinkedList<Byte>>)exits.clone();
                        alljoiners.remove(exit);
                        if (size < targetsize)
                            combinations(alljoiners, extrajoiners);
                        else
                        {
                            extrajoiners.add(alljoiners);
                        }
                    }
                    else
                    {
                        extrajoiners.add(new LinkedList<LinkedList<Byte>>());
                    }

                    for (byte iRegister = 0; iRegister < maxregisters; ++iRegister)
                    {
                        for (byte iNode = -1; iNode < size; ++iNode)
                        {
                            for (LinkedList<LinkedList<Byte>> joiners : extrajoiners)
                            {
                                Graph ggg = (Graph)g.clone();

                                ggg.appendplusnode(exit, iRegister, iNode, joiners);

                                helperfunction(ggg);
                            }
                            for (byte eNode = -1; eNode < size; ++eNode)
                            {
                                for (LinkedList<LinkedList<Byte>> joiners : extrajoiners)
                                {
                                    Graph ggg = (Graph)g.clone();

                                    ggg.appendminusnode(exit, iRegister, iNode, eNode, joiners);

                                    helperfunction(ggg);
                                }
                            }
                        }
                    }
                }
            }
        }
        return completedcalculation;
    }

    private void helperfunction(Graph ggg)
    {
        if (ggg.AllNodesExit())
        {
            ggg.setCode();
            if (!codes.contains(ggg.getCode()))
            {
                boolean bWorthTrying = ggg.initialize();
                if (size < targetsize || bWorthTrying)
                {
                    long c = ++counter;
                    if (c == ((c / 1000000) * 1000000))
                        System.out.println("Calculating " + size + "-node graphs: " + c / 1000000 + " million programs.");

                    int steps = ggg.run();
                    if (steps >= 0)
                    {
                        if (size < targetsize || steps > ancestor.minmaxrunningtime)
                        {
                            try
                            {
                                if (size < targetsize && size <= 6 && codes.size() < 10000000)
                                    codes.add(ggg.getCode());
                                graphs.putLast(ggg);
                            }
                            catch (InterruptedException e)
                            {
                                return;
                            }
                        }
                        if (size <= targetsize)
                        {
                            if (steps > maxrunningtime)
                                winners.clear();
                            if (steps >= maxrunningtime)
                            {
                                maxrunningtime = steps;
                                winners.add(ggg.getLabel());
                                if (maxrunningtime > minmaxrunningtime)
                                    minmaxrunningtime = maxrunningtime;
                            }
                        }
                    }
                }
            }
        }
    }
    
    static private void combinations(LinkedList<LinkedList<Byte>> full, LinkedList<LinkedList<LinkedList<Byte>>> combos)
    {
        if (full.isEmpty())
        {
            combos.clear();
            combos.add(new LinkedList<LinkedList<Byte>>());
        }
        else
        {
        	LinkedList<LinkedList<Byte>> cc = new LinkedList<LinkedList<Byte>>();
            cc.addAll(full);
            LinkedList<Byte> o = cc.removeLast();
            LinkedList<LinkedList<LinkedList<Byte>>> lll = new LinkedList<LinkedList<LinkedList<Byte>>>();
            combinations(cc, lll);
            for (Iterator<LinkedList<LinkedList<Byte>>> iter = lll.iterator(); iter.hasNext(); )
            {
            	LinkedList<LinkedList<Byte>> ll = iter.next();
                combos.add(ll);
                LinkedList<LinkedList<Byte>> l = new LinkedList<LinkedList<Byte>>();
                l.addAll(ll);
                l.add(o);
                combos.add(l);
            }
        }
    }
    
    static void distributions(byte total, byte remaining, LinkedList<LinkedList<Byte>> combos)
    {
        // combos should be empty when this function is called externally.
        // combos will be filled with all the different ways that remaining elements can be distributed into total slots.
        // for example, if called with total=3 and remaining = 4, combos will be set as
        // { {2,1,1}, {1,2,1}, {1,1,2} }

        if (total < 1 || remaining < total)
            return;

        if (total == 1)
        {
            LinkedList<Byte> ccc = new LinkedList<Byte>();
            ccc.add(remaining);
            combos.add(ccc);
            return;
        }

        for (byte i = 1; i <= remaining - total + 1; ++i)
        {
        	LinkedList<LinkedList<Byte>> cccc = new LinkedList<LinkedList<Byte>>();
            distributions((byte)(total-(byte)1), (byte)(remaining-i), cccc);

            /*if (cccc.isEmpty())
            {
                LinkedList ccc = new LinkedList();
                ccc.add(i);
                cccc.add(ccc);
            }
            else*/
            
                for (Iterator<LinkedList<Byte>> iter = cccc.iterator(); iter.hasNext(); )
                {
                	LinkedList<Byte> cc = (LinkedList<Byte>)iter.next();
                    if (i == 1)
                    {
                        cc.add(0,i);
                        combos.add(cc);
                    }
                    else
                    {
                        LinkedList<Byte> ccc = new LinkedList<Byte>();
                        ccc.addAll(cc);
                        ccc.add(0,i);
                        combos.add(ccc);
                    }
                }
            
        }
    }
}
