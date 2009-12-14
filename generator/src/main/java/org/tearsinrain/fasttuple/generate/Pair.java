/*
Copyright (c) 2009 Michael Salib

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

package org.tearsinrain.fasttuple.generate;

import java.util.Iterator;

public class Pair<A, B>
{
    private final A a;
    private final B b;

    public Pair( A a, B b )
    {
        this.a = a;
        this.b = b;
    }

    public A get1(  )
    {
        return a;
    }

    public B get2(  )
    {
        return b;
    }

    private static class Zipper<A, B>
        implements Iterable<Pair<A, B>>
    {
        private final Iterator<Pair<A, B>> thing;

        public Zipper( Iterator<A> aseq, Iterator<B> bseq )
        {
            thing = new Pair.ZipperIt<A, B>( aseq, bseq );
        }

        public Iterator<Pair<A, B>> iterator(  )
        {
            return thing;
        }
    }

    private static class ZipperIt<A, B>
        implements Iterator<Pair<A, B>>
    {
        private final Iterator<A> aseq;
        private final Iterator<B> bseq;

        public ZipperIt( Iterator<A> aseq, Iterator<B> bseq )
        {
            this.aseq = aseq;
            this.bseq = bseq;
        }

        public boolean hasNext(  )
        {
            return aseq.hasNext(  ) && bseq.hasNext(  );
        }

        public Pair<A, B> next(  )
        {
            return new Pair<A, B>( aseq.next(  ),
                                   bseq.next(  ) );
        }

        public void remove(  )
        {
            throw new UnsupportedOperationException(  );
        }
    }

    public static <A, B> Iterable<Pair<A, B>> zip( Iterable<A> aseq, Iterable<B> bseq )
    {
        return new Pair.Zipper<A, B>( aseq.iterator(  ),
                                      bseq.iterator(  ) );
    }
}
