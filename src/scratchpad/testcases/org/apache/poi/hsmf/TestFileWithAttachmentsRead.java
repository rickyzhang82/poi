/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hsmf;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.datatypes.AttachmentChunks;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

/**
 * Tests to verify that we can read attachments from msg file
 * 
 * @author Nicolas Bureau
 */
public class TestFileWithAttachmentsRead extends TestCase {
   private final MAPIMessage twoSimpleAttachments;
   private final MAPIMessage pdfMsgAttachments;

   /**
    * Initialize this test, load up the attachment_test_msg.msg mapi message.
    * 
    * @throws Exception
    */
   public TestFileWithAttachmentsRead() throws IOException {
      POIDataSamples samples = POIDataSamples.getHSMFInstance();
      this.twoSimpleAttachments = new MAPIMessage(samples.openResourceAsStream("attachment_test_msg.msg"));
      this.pdfMsgAttachments = new MAPIMessage(samples.openResourceAsStream("attachment_msg_pdf.msg"));
   }

   /**
    * Test to see if we can retrieve attachments.
    * 
    * @throws ChunkNotFoundException
    * 
    */
   public void testRetrieveAttachments() {
       // Simple file
       AttachmentChunks[] attachments = twoSimpleAttachments.getAttachmentFiles();
       assertEquals(2, attachments.length);
       
       // Other file
       attachments = pdfMsgAttachments.getAttachmentFiles();
       assertEquals(2, attachments.length);
   }

   /**
    * Test to see if attachments are not empty.
    */
   public void testReadAttachments() throws IOException {
      AttachmentChunks[] attachments = twoSimpleAttachments.getAttachmentFiles();

      // Basic checks
      for (AttachmentChunks attachment : attachments) {
         assertTrue(attachment.getAttachFileName().getValue().length() > 0);
         assertTrue(attachment.getAttachLongFileName().getValue().length() > 0);
         assertTrue(attachment.getAttachExtension().getValue().length() > 0);
         if(attachment.getAttachMimeTag() != null) {
            assertTrue(attachment.getAttachMimeTag().getValue().length() > 0);
         }
      }

      AttachmentChunks attachment;

      // Now check in detail
      attachment = twoSimpleAttachments.getAttachmentFiles()[0];
      assertEquals("TEST-U~1.DOC", attachment.getAttachFileName().toString());
      assertEquals("test-unicode.doc", attachment.getAttachLongFileName().toString());
      assertEquals(".doc", attachment.getAttachExtension().getValue());
      assertEquals(null, attachment.getAttachMimeTag());
      assertEquals(24064, attachment.getAttachData().getValue().length);

      attachment = twoSimpleAttachments.getAttachmentFiles()[1];
      assertEquals("pj1.txt", attachment.getAttachFileName().toString());
      assertEquals("pj1.txt", attachment.getAttachLongFileName().toString());
      assertEquals(".txt", attachment.getAttachExtension().getValue());
      assertEquals(null, attachment.getAttachMimeTag());
      assertEquals(89, attachment.getAttachData().getValue().length);
   }
   
   /**
    * Test that we can handle both PDF and MSG attachments
    */
   public void testReadMsgAttachments() throws Exception {
       AttachmentChunks[] attachments = pdfMsgAttachments.getAttachmentFiles();
       assertEquals(2, attachments.length);
       
       AttachmentChunks attachment;

       // Second is a PDF
       attachment = pdfMsgAttachments.getAttachmentFiles()[1];
       assertEquals("smbprn~1.pdf", attachment.getAttachFileName().toString());
       assertEquals("smbprn.00009008.KdcPjl.pdf", attachment.getAttachLongFileName().toString());
       assertEquals(".pdf", attachment.getAttachExtension().getValue());
       assertEquals(null, attachment.getAttachMimeTag());
       assertEquals(null, attachment.getAttachmentDirectory());
       assertEquals(13539, attachment.getAttachData().getValue().length);
       
       // First in a nested message
       attachment = pdfMsgAttachments.getAttachmentFiles()[0];
       assertEquals("Test Attachment", attachment.getAttachFileName().toString());
       assertEquals(null, attachment.getAttachLongFileName());
       assertEquals(null, attachment.getAttachExtension());
       assertEquals(null, attachment.getAttachMimeTag());
       assertEquals(null, attachment.getAttachData());
       assertNotNull(attachment.getAttachmentDirectory());
       
       // Check we can see some bits of it
       MAPIMessage nested = attachment.getAttachmentDirectory().getAsEmbededMessage();
       assertEquals(1, nested.getRecipientNamesList().length);
       assertEquals("Nick Booth", nested.getRecipientNames());
       assertEquals("Test Attachment", nested.getConversationTopic());
   }
}
