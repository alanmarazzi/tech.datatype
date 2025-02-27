package tech.v2.tensor;

import tech.v2.datatype.IntIter;
import tech.v2.datatype.ByteReader;
import clojure.lang.RT;


public interface ByteTensorReader extends ByteReader
{
  byte read2d(int row, int col);
  byte tensorRead(IntIter dims);
  default Object invoke(Object row, Object col) {
    return read2d(RT.intCast(row), RT.intCast(col));
  }
}
