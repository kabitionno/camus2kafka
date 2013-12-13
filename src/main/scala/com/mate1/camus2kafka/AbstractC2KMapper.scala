/*
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */

package com.mate1.camus2kafka

import org.apache.hadoop.mapreduce.Mapper
import org.apache.avro.mapred.AvroKey
import org.apache.avro.generic.{GenericDatumWriter, GenericRecord}
import org.apache.hadoop.io.{Writable, BytesWritable, NullWritable}
import java.io.ByteArrayOutputStream
import org.apache.avro.io.EncoderFactory

/**
 * Created with IntelliJ IDEA.
 * User: Boris Fersing
 * Date: 10/4/13
 * Time: 9:28 AM
 */


/**
 * Abstract class for the C2K Mapper
 * @tparam OUTKEY
 */
abstract class AbstractC2KMapper[OUTKEY <: Writable]
  extends Mapper[AvroKey[GenericRecord], NullWritable, OUTKEY, BytesWritable]
  with C2KJobConfig{

  // Vals that are used by getOutputValue
  val bytesWritableValue = new BytesWritable()
  val out = new ByteArrayOutputStream()
  lazy val writer = new GenericDatumWriter[GenericRecord](C2KJobConfig.outputSchema)
  val encoder = EncoderFactory.get().binaryEncoder(out, null)

  // That type definition make the code easier to read
  type MapperContext = Mapper[AvroKey[GenericRecord], NullWritable, OUTKEY, BytesWritable]#Context


  /**
   * Initializes the C2KJobConfig
   * @param context
   */
  override def setup(context: MapperContext) {
    super.setup(context)
    initConfig(context.getConfiguration)
  }


  /**
   * A default implementation of the map method that uses the getOutputKey and getOutputValue methods
   * @param key
   * @param value
   * @param context
   */
  override def map(key: AvroKey[GenericRecord], value: NullWritable, context: MapperContext) {
    context.write(getOutputKey(key,value), getOutputValue(key,value))
  }


  /**
   * Provide the output value that will be written to the Mapper's context and sent to the Reducer
   * @param key The input key
   * @param value The input value
   * @return The output value that will be sent to the Reducer
   */
  def getOutputValue(key: AvroKey[GenericRecord], value: NullWritable): BytesWritable = {
    out.reset()
    writer.write(key.datum(), encoder)
    encoder.flush()

    val bytesArray = out.toByteArray

    bytesWritableValue.set(bytesArray, 0 , bytesArray.length)
    bytesWritableValue
  }


  /**
   * Provide the output key that will be written to the Mapper's context and sent to the Reducer
   * @param key The input key
   * @param value The input value
   * @return The output key that will be sent to the Reducer
   */
  def getOutputKey(key: AvroKey[GenericRecord], value: NullWritable) : OUTKEY
}