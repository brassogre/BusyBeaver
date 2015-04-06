/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package busybeaver;

/**
 *
 * @author dkutach
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        final int size = 32;
        final int start = 32;
        final int maxregisters = size/2;
        boolean bReported[] = new boolean[size+1];
        GraphList[] graphlists = new GraphList[size+1];
        int nReported = 0;

        for (int s = 0; s < start; ++s)
        {
            bReported[s] = true;
        }

        for (int s = start; s <= size; ++s)
        {
            bReported[s] = false;
            graphlists[s] = new GraphList(s, maxregisters);
            //graphlists[s].run();
            (new Thread(graphlists[s])).start();
        }

        while (nReported < size-start+1)
        {
            for (int ss = start; ss <= size; ++ss)
            {
                if (!bReported[ss])
                {
                    if (graphlists[ss].completed())
                    {
                        bReported[ss] = true;
                        ++nReported;

                        //graphlists[s].printself();
                        System.out.println();
                        System.out.println("r(" + ss + ") = " + graphlists[ss].runningtime());
                        System.out.println("Winners: ");
                        for (String label : graphlists[ss].winners)
                        {
                            System.out.println(label);
                        }
                        System.out.println("The total number of examined graphs of size " + ss + " is " + graphlists[ss].getCounter());
                        graphlists[ss] = null;
                    }
                }
            }
        }
        
        try
        {
            Thread.sleep(50);
        }
        catch (InterruptedException e)
        {
            System.err.println("The main thread was interupted.");
        }
    }

    /**
     * This private method is used to find a busy beaver without assuming it
     * takes the standard form of adders and then subtracters nested linearly.
     */
    @SuppressWarnings("unused")
	private void ExhaustiveSearch()
    {
        int size = 7;
        boolean bFinished = false;

        System.out.println("Calculating Running Time r(" + size + ")....");

        try
        {
            GraphList glist1 = new GraphList();

            System.out.println();
            System.out.println("r(1) = " + glist1.runningtime());
            System.out.println("Winners: ");
            for (String label : glist1.winners)
            {
                System.out.println(label);
            }

            GraphList glist2 = new GraphList(glist1, size);

            do
            {
                bFinished = glist2.grow();
            } while (!bFinished);

            System.out.println();
            System.out.println("r(2) = " + glist2.runningtime());
            System.out.println("Winners: ");
            for (String label : glist2.winners)
            {
                System.out.println(label);
            }

            GraphList glist3 = new GraphList(glist2, size);

            do
            {
                bFinished = glist3.grow();
            } while (!bFinished);

            System.out.println();
            System.out.println("r(3) = " + glist3.runningtime());
            System.out.println("Winners: ");
            for (String label : glist3.winners)
            {
                System.out.println(label);
            }
            System.out.println("The total number of unique graphs is " + glist3.getCounter());

            GraphList glist4 = new GraphList(glist3, size);

            do
            {
                bFinished = glist4.grow();
            } while (!bFinished);

            System.out.println();
            System.out.println("r(4) = " + glist4.runningtime());
            System.out.println("Winners: ");
            for (String label : glist4.winners)
            {
                System.out.println(label);
            }
            System.out.println("The total number of unique graphs is " + glist4.getCounter());

            GraphList glist5 = new GraphList(glist4, size);

            for (int t = 0; t < 1; ++t)
                (new Thread(glist5)).start();

            GraphList glist6 = new GraphList(glist5, size);

            for (int t = 0; t < 1; ++t)
                (new Thread(glist6)).start();


            GraphList glist7 = new GraphList(glist6, size);

            for (int t = 0; t < 16; ++t)
                (new Thread(glist7)).start();

            boolean bReported5 = false;
            boolean bReported6 = false;
            boolean bReported7 = false;
            // boolean bReported8 = false;

            while (!bReported7)
            {
                if (glist5.completed())
                {
                    if (!bReported5)
                    {
                        //glist4 = null;
                        bReported5 = true;
                        System.out.println();
                        System.out.println("r(5) = " + glist5.runningtime());
                        System.out.println("Winners: ");
                        for (String label : glist5.winners)
                        {
                            System.out.println(label);
                        }
                        System.out.println("The total number of unique graphs is " + glist5.getCounter());
                    }
                }

                if (glist6.completed())
                {
                    //if (bReported5)
                        //glist5 = null;

                    if (!bReported6)
                    {
                        bReported6 = true;
                        System.out.println();
                        System.out.println("r(6) = " + glist6.runningtime());
                        System.out.println("Winners: ");
                        for (String label : glist6.winners)
                        {
                            System.out.println(label);
                        }
                        System.out.println("The total number of unique graphs is " + glist6.getCounter());
                    }
                }

                if (glist7.completed())
                {
                    //if (bReported6)
                        //glist6 = null;

                    if (!bReported7)
                    {
                        bReported7 = true;
                        System.out.println();
                        System.out.println("r(7) = " + glist7.runningtime());
                        System.out.println("Winners: ");
                        for (String label : glist7.winners)
                        {
                            System.out.println(label);
                        }
                        System.out.println("The total number of unique graphs is " + glist7.getCounter());
                    }
                }

                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    System.err.println("The main thread was interupted.");
                    //return;
                }
            }

            /*do
            {
                bFinished = glist4.grow();
            } while (!bFinished);*/

/*            GraphList glist5 = new GraphList(glist4, size);

            do
            {
                bFinished = glist5.grow();
            } while (!bFinished);

            System.out.println("r("+ size + ") = " + glist5.runningtime());
            System.out.println("Winners: ");
            for (String label : glist5.winners)
            {
                System.out.println(label);
            }*/

        }
        catch (CloneNotSupportedException e)
        {
            System.err.println("The program failed due to an internal error.");
        }
    }
}

