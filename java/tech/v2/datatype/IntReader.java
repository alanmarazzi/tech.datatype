package tech.v2.datatype;

import clojure.lang.IFn;
import clojure.lang.Keyword;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;
import clojure.lang.RT;


public interface IntReader extends IOBase, Iterable, IFn, List, RandomAccess
{
  int read(long idx);
  default Keyword getDatatype () { return Keyword.intern(null, "int32"); }
  default int size() { return RT.intCast(lsize()); }
  default Object get(int idx) { return read(idx); }
  default boolean isEmpty() { return lsize() == 0; }
  default Iterator iterator() {
    return new IntReaderIter(this);
  }
  default Object invoke(Object arg) {
    return read(RT.longCast(arg));
  }
}
