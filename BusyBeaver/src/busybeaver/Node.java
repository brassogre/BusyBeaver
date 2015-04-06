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
public class Node implements Cloneable {
    
    final private byte name;
    final private boolean nodetype; /* false => increment node, true => decrement node */
    final private byte register;
    private byte nextIndex;
    private Node next;
    private byte emptyIndex;
    private Node empty;

    public Node(int n)
    {
        name = 0;
        register = 0;
        next = null;
        nextIndex = -1;
        empty = null;
        emptyIndex = -1;
        nodetype = (n != 1);
        if (n == 2)
        {
            next = this;
            nextIndex = name;
        }
    }
    public Node(byte n, byte reg, byte iNext, Node pNext)
    {
        name = n;
        register = reg;
        nodetype = false;
        next = null;
        nextIndex = iNext;
        if (nextIndex >= 0)
        {
            if (pNext == null)
                pNext = this;
            next = pNext;
        }
        empty = null;
        emptyIndex = -1;
     }
    public Node(byte n, byte reg, byte iNext, Node pNext, byte iEmpty, Node pEmpty)
    {
        name = n;
        register = reg;
        nodetype = true;
        next = null;
        nextIndex = iNext;
        if (nextIndex >= 0)
        {
            if (pNext == null)
                pNext = this;
            next = pNext;
        }
        empty = null;
        emptyIndex = iEmpty;
        if (emptyIndex >= 0)
        {
            if (pEmpty == null)
                pEmpty = this;
            empty = pEmpty;
        }
     }

     public void getIndexMappings(ArrayList<Byte> indexmappings)
     {
         if (!indexmappings.contains(name))
         {
             indexmappings.add(name);
             if (empty != null)
                 empty.getIndexMappings(indexmappings);
             if (next != null)
                 next.getIndexMappings(indexmappings);
         }
     }
     public void getCode(ArrayList<Node> nodes, ArrayList<Byte> registermappings, ArrayList<Byte> indexmappings, ArrayList<Byte> output)
     {
         if (!nodes.contains(this))
         {
             nodes.add(this);

             // Let reg represent this's register, mapped to another register if registermappings has already included another register.
             // if registermappings = { 0, 2 }
             // that means if register == 0 this should list itself as 0, if register == 2 this should list itself as 1,
             // if register == 1 this should first add a 1 to the end of registermappings and then list itself as 2.
             byte reg = 0;
             byte ind = 0;
             boolean bMapped = false;
             Iterator<Byte> iter = registermappings.iterator();
             while (iter.hasNext() && ind <= register)
             {
                 byte r = (byte)iter.next();
                 if (register == r)
                 {
                     reg = ind;
                     bMapped = true;
                 }
                 ++ind;
             }
             if (!bMapped)
             {
                 registermappings.add(register);
                 reg = ind;
             }

             Byte b = (byte) (reg & 3);
             if (nodetype)
                 b = (byte) (b + 4);
             output.add(b);

             // Adjust references to other nodes

             if (nodetype)
             {
                 bMapped = false;
                 iter = indexmappings.iterator();
                 ind = 0;
                 byte eIndex = 0;

                 while (iter.hasNext() && !bMapped)
                 {
                     byte i = (byte)iter.next();
                     if (emptyIndex == i)
                     {
                         eIndex = ind;
                         bMapped = true;
                     }
                     ++ind;
                 }

                 assert(bMapped);
                 b = (byte) (eIndex & 7);
                 output.add(b);
             }

             bMapped = false;
             iter = indexmappings.iterator();
             ind = 0;
             byte nIndex = 0;

             while (iter.hasNext() && !bMapped)
             {
                 byte i = (byte)iter.next();
                 if (nextIndex == i)
                 {
                     nIndex = ind;
                     bMapped = true;
                 }
                 ++ind;
             }

             assert(bMapped);

             b = (byte) (nIndex & 7);
             output.add(b);

             if (empty != null)
             {
                 if (!nodes.contains(empty))
                     empty.getCode(nodes, registermappings, indexmappings, output);
             }

             if (next != null)
             {
                 if (!nodes.contains(next))
                    next.getCode(nodes, registermappings, indexmappings, output);
             }
         }
     }
    /*public void SetAsPlus(int reg, int ni, Node n)
    {
        nodetype = false;
        register = reg;
        next = n;
        nextIndex = ni;
        empty = null;
        emptyIndex = -1;
    }
    public void SetAsMinus(int reg, int ni, Node n, int ei, Node e)
    {
        nodetype = true;
        register = reg;
        next = n;
        nextIndex = ni;
        empty = e;
        emptyIndex = ei;
    }*/

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    /*public boolean verifyConsistency(ArrayList<Node> nodelist)
    {
        if (nextIndex < 0 && next != null)
            return false;
        if ((nextIndex >= 0) && (next == null))
            return false;
        if (next != null && nodelist.get(nextIndex) != next)
            return false;
        if ((emptyIndex < 0) && (empty != null))
            return false;
        if ((emptyIndex >= 0) && (empty == null))
            return false;
        if (empty != null && nodelist.get(emptyIndex) != empty)
            return false;
        return true;
    }*/
    public void fixreferences(ArrayList<Node> nodelist)
    {
        if (nextIndex >= 0)
            next = nodelist.get(nextIndex);
        if (emptyIndex >= 0)
            empty = nodelist.get(emptyIndex);
    }

    /*protected boolean isEquivalentTo(Node n, ArrayList<Integer> nodemappings, Set visitednodes)
    {
        visitednodes.add(this);
        if (n.nodetype != nodetype)
            return false;
        if (n.next == null && next != null)
            return false;
        if (n.next != null && next == null)
            return false;
        if (n.empty == null && empty != null)
            return false;
        if (n.empty != null && empty == null)
            return false;
        if (n.register != nodemappings.get(register))
            return false;
        if (next != null && !visitednodes.contains(next))
        {
            if (!next.isEquivalentTo(n.next, nodemappings, visitednodes))
                return false;
        }
        if (empty != null && !visitednodes.contains(empty))
        {
            if (!empty.isEquivalentTo(n.empty, nodemappings, visitednodes))
                return false;
        }
        return true;
    }*/
    public byte getRegister()
    {
        return register;
    }
    public void getRegister(Set<Byte> s, ArrayList<Byte> indexmappings)
    {
        if (!s.contains(register))
            indexmappings.add(register);
    }
    public byte getRegisters(Set<Byte> incregs, Set<Byte> decregs, Integer nExits)
    {
        if (nodetype)
            decregs.add(register);
        else
            incregs.add(register);
        if (next == null)
            ++nExits;
        if (nodetype && empty == null)
            ++nExits;
        return register;
    }
    public String getString(ArrayList<Byte> indexmappings)
    {
        assert(nodetype || (next != this && nextIndex != name));

        String s = nodetype ? "-" : "+";

        if (indexmappings == null)
        {
            s += Integer.toString(register+1);
        }
        else
        {
            assert(indexmappings.size() < 4);
            for (int i = 0; i < indexmappings.size(); ++i)
            {
                if (register == indexmappings.get(i))
                {
                    s += Integer.toString(i+1);
                    i = indexmappings.size();
                }
            }
        }

        if (nodetype)
        {
            s += "{";
            if (empty == null)
                s += "X";
            else
                s += Integer.toString(emptyIndex+1);
            s += "}";
        }
        s += "[";
        if (next == null)
            s += "X";
        else
            s += Integer.toString(nextIndex+1);
        s += "]";

        return s;
    }
    public void exitarrows(LinkedList<LinkedList<Byte>> exits)
    {
        if (next == null)
        {
            LinkedList<Byte> exitarrow = new LinkedList<Byte>();
            exitarrow.add(name);
            exitarrow.add(new Byte((byte) 0)); // Representing false
            exits.add(exitarrow);
        }
        if (nodetype && empty == null)
        {
            LinkedList<Byte> exitarrow = new LinkedList<Byte>();
            exitarrow.add(name);
            exitarrow.add(new Byte((byte) 1)); // Representing true
            exits.add(exitarrow);
        }
    }
    public void join(byte ni, Node n, boolean bEmpty)
    {
        assert (n != null);
        assert (ni >= 0);
        if (bEmpty)
        {
            assert(nodetype && empty == null && emptyIndex < 0);
            if (nodetype && empty == null)
            {
                emptyIndex = ni;
                empty = n;
            }
        }
         else
        {
            assert(nodetype || (n != this && ni != name));
            assert(next == null && nextIndex < 0);
            if (next == null)
            {
                nextIndex = ni;
                next = n;
            }
        }
    }
    public boolean hasExit(Set<Byte> sCheckedNodes, Set<Byte> sNodesWithExits)
    {
        if (sCheckedNodes.contains(name))
            return sNodesWithExits.contains(name);

        sCheckedNodes.add(name);

        boolean retval = false;

        if (nodetype)
        {
            if (empty == this) // This line is included because it never helps to have decrement nodes where the e exit loops back to this very node.
                return false; // Such a node would always get trapped in an infinite loop if the register were ever empty.

            if (empty == null || empty.hasExit(sCheckedNodes, sNodesWithExits))
            {
                sNodesWithExits.add(name);
                retval = true;
            }
        }

        if (next == null || next.hasExit(sCheckedNodes, sNodesWithExits))
        {
            sNodesWithExits.add(name);
            retval = true;
        }

        return retval;
    }
    public void printself()
    {
        String ccc = (nodetype) ? "-" : "+";
        String ddd = (next != null) ? String.valueOf(nextIndex+1) : "X";
        String eee = "";
        if (nodetype)
            eee = (empty != null) ? ","+String.valueOf(emptyIndex+1) : ",X";
        System.out.print(String.valueOf(name+1)+"("+String.valueOf(register+1)+ccc+")->["+ddd+eee+"] ");
    }
    public byte PerformStep(ArrayList<Integer> state)
    {
        if (nodetype)
        {
            /* decrement node */
            int k = (state.get(register+1));
            if (k == 0)
            {
                state.set(0, Integer.valueOf(emptyIndex));
                return emptyIndex;
            }
            state.set(register+1, k-1);
            state.set(0, Integer.valueOf(nextIndex));
            return nextIndex;
        }

        /* increment node */
        int k = state.get(register+1);
        state.set(register+1, k+1);
        state.set(0, Integer.valueOf(nextIndex));
        return nextIndex;
    }
}

