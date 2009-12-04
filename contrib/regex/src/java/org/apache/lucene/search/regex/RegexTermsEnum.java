package org.apache.lucene.search.regex;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.search.FilteredTermsEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermRef;

import java.io.IOException;

/**
 * Subclass of FilteredTermsEnum for enumerating all terms that match the
 * specified regular expression term using the specified regular expression
 * implementation.
 * <p>
 * Term enumerations are always ordered by Term.compareTo().  Each term in
 * the enumeration is greater than all that precede it.
 *
 * @deprecated Use {@link RegexTermsEnum} instead.
 */

public class RegexTermsEnum extends FilteredTermsEnum {
  private String field = "";
  private String pre = "";
  private final boolean empty;
  private RegexCapabilities regexImpl;
  private final TermRef prefixRef;

  public RegexTermsEnum(IndexReader reader, Term term, RegexCapabilities regexImpl) throws IOException {
    super();
    field = term.field();
    String text = term.text();
    this.regexImpl = regexImpl;

    regexImpl.compile(text);

    pre = regexImpl.prefix();
    if (pre == null) pre = "";

    Terms terms = reader.fields().terms(term.field());
    prefixRef = new TermRef(pre);
    if (terms != null) {
      empty = setEnum(terms.iterator(), prefixRef) == null;
    } else {
      empty = true;
    }
  }

  public String field() {
    return field;
  }

  protected final AcceptStatus accept(TermRef term) {
    if (term.startsWith(prefixRef)) {
      return regexImpl.match(term.toString()) ? AcceptStatus.YES : AcceptStatus.NO;
    } else {
      return AcceptStatus.END;
    }
  }

  public final float difference() {
// TODO: adjust difference based on distance of searchTerm.text() and term().text()
    return 1.0f;
  }

  public final boolean empty() {
    return empty;
  }
}