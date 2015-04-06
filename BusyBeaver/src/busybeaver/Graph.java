/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package busybeaver;

import java.util.*;
        
/**
 *
 * @author dkutach
 */
public class Graph implements Cloneable {
    private byte size;
    private byte registers;
    protected ArrayList<Node> nodelist;
    protected long code;
    private static long maxSteps = 2000000;

    public Graph(byte n)
    {
        size = 1;
        nodelist = new ArrayList<Node>();
        nodelist.add(new Node(n));
    }

    public Graph(LinkedList<Byte> sizes)
    {
        registers = (byte)sizes.size();
        size = registers;

        for (Byte s : sizes) {
            size += s;
        }

        nodelist = new ArrayList<Node>();
        byte ss = size;
        byte iNext = -1;
        Node pNext = null;
        byte iEmpty = -1;
        Node pEmpty = null;
        for (byte r = registers; r > 0; )
        {
            --r;
            --ss;
            Node n = new Node(ss, r, (byte)(-1), null, iEmpty, pEmpty);
            nodelist.add(0, n);
            iEmpty = ss;
            iNext = ss;
            pEmpty = n;
            pNext = n;
        }

        byte r = registers;
        for (Iterator<Byte> iter = sizes.descendingIterator(); iter.hasNext(); )
        {
            --r;
            byte s = (Byte)iter.next();
            while (s-- > 0)
            {
                --ss;
                assert(ss != iNext);
                Node n = new Node(ss, r, iNext, pNext);
                nodelist.add(0, n);
                iNext = ss;
                pNext = n;
            }
        }

        // Assign recently created increment nodes with targets for their non-e arrows.
        byte[] targets = new byte[registers];
        for (byte i = 0; i < size - registers; ++i)
        {
            byte reg = nodelist.get(i).getRegister();
            targets[reg] = (byte)(i+(byte)1);
        }

        r = registers;
        byte index = size;
        while (--r >= 0)
        {
            --index;
            Node n = nodelist.get(index);
            Node t = nodelist.get(targets[r]);
            n.join(targets[r], t, false);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        Graph newGraph = (Graph)super.clone();

        ArrayList<Node> newnodelist = new ArrayList<Node>();
        for (Node n : nodelist)
        {
            newnodelist.add((Node)n.clone());
        }
        for (Node n : newnodelist)
        {
            n.fixreferences(newnodelist);
        }

        newGraph.nodelist = newnodelist;

        //if (!newGraph.verifyConsistency())
         //   System.err.println("Clone of Graph malfunctioned.");

        return newGraph;
    }
    /*protected boolean verifyConsistency()
    {
        boolean consistent = true;
        for (Node n : nodelist)
        {
            consistent &= n.verifyConsistency(nodelist);
        }
        return consistent;
    }*/
    public boolean initialize()
    {
        //assert(nodelist.size() == size);
        //assert(verifyConsistency());

        Set<Byte> IncrementRegisters = new TreeSet<Byte>();
        Set<Byte> DecrementRegisters = new TreeSet<Byte>();
        registers = 0;
        Integer nExits = 0;
        for (Node n : nodelist)
        {
            byte reg = (byte) (n.getRegisters(IncrementRegisters, DecrementRegisters, nExits) + ((byte) 1));
            if (reg > registers)
                registers = reg;
        }

        // nExits == 1 && 
        return (size <= 2 || ((size >= registers * 2) && registers == IncrementRegisters.size() && IncrementRegisters.size() + 1 >= (size/2) && DecrementRegisters.containsAll(IncrementRegisters)));
    }
    protected void setCode()
    {
        code = 0;
        ArrayList<Byte> codebytes = new ArrayList<Byte>();
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Byte> indexmappings = new ArrayList<Byte>();
        ArrayList<Byte> registermappings = new ArrayList<Byte>();

        if (!nodelist.isEmpty())
        {
            Node entry = nodelist.get(0);

            entry.getIndexMappings(indexmappings);

            entry.getCode(nodes, registermappings, indexmappings, codebytes);

            assert(nodes.size() == size);

            for (byte b : codebytes)
            {
                code = Long.rotateLeft(code, 3);
                code += (b & 7);
            }
        }
    }
    public long getCode()
    {
        return code;
    }
    protected String getLabel()
    {
        String label = "";

        ArrayList<Byte> indexmappings = new ArrayList<Byte>();
        HashSet<Byte> s = new HashSet<Byte>();
        for (int i = 0; i < size; ++i)
        {
            Node n = nodelist.get(i);
            n.getRegister(s, indexmappings);
        }

        for (int i = 0; i < size; )
        {
            Node n = nodelist.get(i++);
            label += n.getString(indexmappings);
            if (i < size)
                label += ",";
        }
        return label;
    }
    protected String getUnalteredLabel()
    {
        String label = "";
        for (int i = 0; i < size; )
        {
            Node n = nodelist.get(i++);
            label += n.getString(null);
            if (i < size)
                label += ",";
        }
        return label;
    }
    /*protected boolean hasSameLabel(Graph g)
    {
        return (g != null && (g.label == null ? label == null : g.label.equals(label)));
    }*/
    protected boolean hasSameCode(Graph g)
    {
        return (g != null && code == g.code);
    }
    public int nodes()
    {
        return nodelist.size();
    }

    public int run()
    {
        long max = 10000;
        if (size >= 20)
            max = 100000;
        if (size >= 26)
            max = maxSteps;

        return run(max);
    }

    public int run(long max)
    {
        //assert(nodelist.size() == size);
        //assert(verifyConsistency());

        int step = 0;
        int currentnodeindex = 0;
        //ArrayList<ArrayList<Integer>> states = new ArrayList<ArrayList<Integer>>();
        boolean bInfiniteLoop = false;

        ArrayList<Integer> state = new ArrayList<Integer>();
        state.add(currentnodeindex);
        for (int i = 0; i < registers; ++i)
        {
            state.add(0);
        }
        //states.add((ArrayList<Integer>) state.clone());

        while (currentnodeindex >= 0 && !bInfiniteLoop && step < max)
        {
            assert(currentnodeindex < size);
            Node currentNode = nodelist.get(currentnodeindex);

            //assert(currentNode.verifyConsistency(nodelist));

            currentnodeindex = currentNode.PerformStep(state);
            ++step;

            /*for (ArrayList<Integer> test : states)
            {
                boolean bSame = true;
                for (int i = 0; i < test.size(); ++i)
                {
                    bSame &= (test.get(i) == state.get(i));
                }
                bInfiniteLoop |= bSame;
            }*/

            //states.add((ArrayList<Integer>)state.clone());
        }
        if (bInfiniteLoop)
            step = -1;
        if (step >= max)
            step = -2;
        if (step > 0)
        {
            boolean bAllBlankRegisters = true;
            Iterator<Integer> iter = state.iterator();
            assert(iter.hasNext());
            for (iter.next(); bAllBlankRegisters && iter.hasNext(); )
            {
                int reg = (Integer)iter.next();
                bAllBlankRegisters &= (reg == 0);
            }
            if (!bAllBlankRegisters)
                step = 0;
            /*statistics.ensureCapacity(step);
            while (statistics.size() <= step)
                statistics.add(0);
            int i = statistics.get(step);
            statistics.set(step, i+1);*/
        }
        return step;
    }

    public void printself()
    {
        System.out.print("Graph (" + getUnalteredLabel() + ")");
        //for (int i = 0; i < size; ++i)
        //{
        //    ((Node)(nodelist.get(i))).printself();
        //}

        System.out.println();
    }
    
    public boolean AllNodesExit()
            /* Returns true iff the graph uses all its nodes and all nodes have an exit*/
    {
        Set<Byte> sTemp = new HashSet<Byte>();
        Set<Byte> sNodesWithExits = new HashSet<Byte>();

        int nodes = nodelist.size();
        if (nodes > 0)
        {
            Node entry = nodelist.get(0);
            entry.hasExit(sTemp, sNodesWithExits);
            return (sNodesWithExits.size() == nodes);
        }
        return true;
    }

    /*protected boolean isEquivalentTo(Graph g)
    {
        if (g.nodes() != nodes())
            return false;

        if (nodes() == 0)
            return true;

        ArrayList<Integer> nodemappings = new ArrayList<Integer>();
        HashSet<Node> visitednodes = new HashSet<Node>();

        Node entry = (Node)nodelist.get(0);
        return entry.isEquivalentTo(g, nodemappings, visitednodes);
    }*/
    
    public int exitarrows()
    {
    	LinkedList<LinkedList<Byte>> exits = new LinkedList<LinkedList<Byte>>();
        exitarrows(exits);
        return exits.size();
    }

    public void exitarrows(LinkedList<LinkedList<Byte>> exits)
    {
        for (Node n : nodelist)
            n.exitarrows(exits);
    }

    public void appendplusnode(LinkedList<Byte> exit, byte iRegister, byte iNode, LinkedList<LinkedList<Byte>> extrajoiners)
    {
        byte n = (byte) nodelist.size();
        assert(iNode >= 0 && iNode < n);

        Node plusreferent = null;
        if (iNode >= 0 && iNode < n)
            plusreferent = nodelist.get(iNode);
                
        Node newnode = new Node(n, iRegister, iNode, plusreferent);
        nodelist.add(newnode);
        ++size;

        byte iOldnode = (Byte)exit.peekFirst();
        boolean bEmptyArrow = (Boolean)(exit.peekLast() == (byte)1);
        Node Oldnode = nodelist.get(iOldnode);
        Oldnode.join(n,newnode,bEmptyArrow);

        for (Iterator<LinkedList<Byte>> iter = extrajoiners.iterator(); iter.hasNext(); )
        {
            LinkedList<Byte> joiner = iter.next();
            assert(joiner.size() == 2);

            byte iJoiner = (Byte)joiner.peekFirst();
            bEmptyArrow = (Boolean)(joiner.peekLast() == (byte)1);
            Node joinnode = nodelist.get(iJoiner);
            joinnode.join(n,newnode,bEmptyArrow);
        }
        setCode();
    }

    public void appendminusnode(LinkedList<Byte> exit, byte iRegister, byte iNode, byte eNode, LinkedList<LinkedList<Byte>> extrajoiners)
    {
        byte n = (byte) nodelist.size();
        assert(iNode >= 0 && iNode <= n);
        assert(eNode >= 0 && eNode <= n);

        Node minusreferent = null;
        if (iNode >= 0 && iNode < n)
            minusreferent = nodelist.get(iNode);
        Node emptyreferent = null;
        if (eNode >= 0 && eNode < n)
            emptyreferent = nodelist.get(eNode);

        Node newnode = new Node(n, iRegister, iNode, minusreferent, eNode, emptyreferent);
        nodelist.add(newnode);
        ++size;

        byte iOldNode = (Byte)exit.peekFirst();
        assert(iOldNode >= 0);
        Node oldnode = nodelist.get(iOldNode);
        boolean bOldNode = (Boolean)(exit.peekLast() == (byte)1);
        oldnode.join(n,newnode,bOldNode);

        for (Iterator<LinkedList<Byte>> iter = extrajoiners.iterator(); iter.hasNext(); )
        {
            LinkedList<Byte> joiner = iter.next();
            assert(joiner.size() == 2);
            {
                byte iJoiner = (Byte)joiner.peekFirst();
                boolean bEmptyArrow = (Boolean)(joiner.peekLast() == (byte)1);
                Node joinnode = nodelist.get(iJoiner);
                joinnode.join(n,newnode,bEmptyArrow);
            }
        }
        setCode();
    }

}
