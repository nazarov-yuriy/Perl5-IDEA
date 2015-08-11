/* The following code was generated by JFlex 1.4.3 on 11.08.15 23:16 */

/*
    Copyright 2015 Alexandr Evstigneev

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.perl5.lang.perl.lexer;

import com.intellij.psi.tree.IElementType;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.4.3
 * on 11.08.15 23:16 from the specification file
 * <tt>C:/Repository/Perl5-IDEA/src/com/perl5/lang/perl/lexer/PerlString.flex</tt>
 */
public abstract class PerlStringLexerGenerated extends PerlBaseLexer
{
  /**
   * lexical states
   */
  public static final int YYINITIAL = 0;
  public static final int LEX_ESCAPED = 2;
  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;
  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
          0,  0,  1, 1
  };

  /** 
   * Translates characters to character classes
   */
  private static final String ZZ_CMAP_PACKED =
          "\41\0\1\23\1\45\1\37\1\36\1\30\1\31\1\4\2\0\1\27" +
                  "\1\6\1\22\1\17\1\10\1\26\1\12\1\16\10\11\1\3\1\25" +
                  "\1\21\1\34\1\20\1\24\1\35\4\14\1\5\1\14\24\1\1\42" +
                  "\1\46\1\43\1\2\1\7\1\44\1\14\1\15\2\14\1\5\1\14" +
                  "\21\1\1\13\2\1\1\40\1\32\1\41\1\33\uff81\0";

  /** 
   * Translates characters to character classes
   */
  private static final char[] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);
  private static final String ZZ_ACTION_PACKED_0 =
          "\2\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7" +
                  "\2\10\1\11\1\12\1\13\1\14\1\15\1\16\1\17" +
                  "\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27" +
                  "\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37" +
                  "\1\40\2\0\1\41\1\42\1\0\1\43\1\2\2\43" +
                  "\3\2\1\0\1\44\1\45\1\46\1\47\2\0\1\42" +
                  "\1\0\4\43";
  /**
   * Translates DFA states to action switch labels.
   */
  private static final int[] ZZ_ACTION = zzUnpackAction();
  private static final String ZZ_ROWMAP_PACKED_0 =
          "\0\0\0\47\0\116\0\165\0\234\0\303\0\116\0\116" +
                  "\0\352\0\u0111\0\u0138\0\u015f\0\116\0\116\0\116\0\116" +
                  "\0\116\0\116\0\116\0\116\0\116\0\116\0\116\0\116" +
                  "\0\116\0\116\0\u0186\0\116\0\116\0\116\0\116\0\116" +
                  "\0\116\0\116\0\116\0\u01ad\0\u01d4\0\234\0\u01fb\0\u0222" +
                  "\0\u0249\0\u0270\0\u0297\0\352\0\u02be\0\u02e5\0\u030c\0\u0333" +
                  "\0\116\0\116\0\u035a\0\u035a\0\u0381\0\303\0\u03a8\0\u03cf" +
                  "\0\u03cf\0\u03f6\0\u02be\0\u02e5";
  /**
   * Translates a state to a row index in the transition table
   */
  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();
  private static final String ZZ_TRANS_PACKED_0 =
          "\1\3\1\4\1\5\1\6\1\7\1\4\1\10\1\4" +
                  "\1\11\1\12\1\13\3\4\1\12\1\14\1\15\1\16" +
                  "\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26" +
                  "\1\27\1\30\1\31\1\32\1\33\1\3\1\34\1\35" +
                  "\1\36\1\37\1\40\1\41\1\42\47\43\50\0\1\4" +
                  "\1\0\1\44\1\45\1\4\1\0\1\4\1\0\6\4" +
                  "\31\0\1\46\3\0\1\46\1\0\1\46\1\0\6\46" +
                  "\33\0\1\47\50\0\1\50\3\0\2\51\3\0\1\51" +
                  "\31\0\1\4\1\0\1\44\1\45\1\52\1\0\1\53" +
                  "\1\54\2\12\3\4\1\12\31\0\1\4\1\0\1\44" +
                  "\1\45\1\52\1\0\1\53\1\54\2\12\1\55\1\4" +
                  "\1\56\1\12\31\0\1\57\3\0\1\57\1\0\1\57" +
                  "\1\0\6\57\1\60\1\61\65\0\1\62\12\0\1\63" +
                  "\44\0\1\64\3\0\1\64\1\0\1\64\1\0\6\64" +
                  "\31\0\1\65\1\0\1\66\1\67\1\65\1\0\1\65" +
                  "\1\0\6\65\36\0\1\70\1\71\1\0\2\71\3\0" +
                  "\1\71\1\70\34\0\1\50\1\0\1\51\1\0\2\51" +
                  "\3\0\1\51\31\0\1\4\1\0\1\44\1\45\1\4" +
                  "\1\70\1\72\1\0\2\72\3\4\1\72\1\70\30\0" +
                  "\1\4\1\0\1\44\1\45\1\52\1\0\1\53\1\54" +
                  "\2\53\3\4\1\53\31\0\1\4\1\0\1\44\1\45" +
                  "\1\73\1\0\1\4\1\0\2\73\1\4\3\73\31\0" +
                  "\1\4\1\0\1\44\1\45\1\4\1\0\1\4\1\0" +
                  "\1\4\1\74\3\4\1\74\31\0\1\57\3\0\1\57" +
                  "\1\0\1\57\1\0\6\57\31\0\1\57\3\0\1\57" +
                  "\1\0\1\57\1\0\6\57\1\60\30\0\1\64\1\0" +
                  "\1\44\1\45\1\64\1\0\1\64\1\0\6\64\31\0" +
                  "\1\65\1\0\1\44\1\45\1\65\1\0\1\65\1\0" +
                  "\6\65\31\0\1\65\3\0\1\65\1\0\1\65\1\0" +
                  "\6\65\37\0\1\71\1\0\2\71\3\0\1\71\31\0" +
                  "\1\4\1\0\1\44\1\45\1\4\1\0\1\72\1\0" +
                  "\2\72\3\4\1\72\30\0";
  /**
   * The transition table of the DFA
   */
  private static final int[] ZZ_TRANS = zzUnpackTrans();
  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;
  private static final char[] EMPTY_BUFFER = new char[0];
  private static final int YYEOF = -1;
  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
          "Unkown internal scanner error",
          "Error: could not match input",
          "Error: pushback value was too large"
  };
  private static final String ZZ_ATTRIBUTE_PACKED_0 =
          "\2\0\1\11\3\1\2\11\4\1\16\11\1\1\10\11" +
                  "\2\0\2\1\1\0\7\1\1\0\2\11\2\1\2\0"+
    "\1\1\1\0\4\1";
  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();
  private static java.io.Reader zzReader = null; // Fake
  /** the current state of the DFA */
  private int zzState;
  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;
  /** this buffer contains the current text to be matched and is
   the source of the yytext() string */
  private CharSequence zzBuffer = "";
  /** this buffer may contains the current text array to be matched when it is cheap to acquire it */
  private char[] zzBufferArray;
  /** the textposition at the last accepting state */
  private int zzMarkedPos;
  /** the textposition at the last state to be included in yytext */
  private int zzPushbackPos;
  /** the current text position in the buffer */
  private int zzCurrentPos;
  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;
  /** endRead marks the last character in the buffer, that has been read
   from input */
  private int zzEndRead;
  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;
  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  public PerlStringLexerGenerated(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Creates a new scanner.
   * There is also java.io.Reader version of this constructor.
   *
   * @param   in  the java.io.Inputstream to read input from.
   */
  public PerlStringLexerGenerated(java.io.InputStream in) {
    this(new java.io.InputStreamReader(in));
  }

  private static int [] zzUnpackAction()
  {
    int[] result = new int[60];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  private static int [] zzUnpackRowMap()
  {
    int[] result = new int[60];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  private static int [] zzUnpackTrans()
  {
    int[] result = new int[1053];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  private static int [] zzUnpackAttribute()
  {
    int[] result = new int[60];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /**
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed)
  {
    char[] map = new char[0x10000];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < 96) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public CharSequence getBuffer()
  {
    return zzBuffer;
  }

  public char[] getBufferArray()
  {
    return zzBufferArray;
  }

  public int getBufferEnd()
  {
    return zzEndRead;
  }

  public int getNextTokenStart()
  {
    return zzMarkedPos;
  }

  public boolean isLastToken()
  {
    return zzMarkedPos == zzEndRead;
  }

  public final int getTokenStart(){
    return zzStartRead;
  }

  /* user code: */
  //fixme this must be in skeleton
  public void setTokenStart(int position)
  {
    zzCurrentPos = zzStartRead = position;
  }

  public final int getTokenEnd(){
    return getTokenStart() + yylength();
  }

  public void setTokenEnd(int position)
  {
    zzMarkedPos = position;}

  public void reset(CharSequence buffer, int start, int end,int initialState){
    zzBuffer = buffer;
    zzBufferArray = com.intellij.util.text.CharArrayUtil.fromSequenceWithoutCopying(buffer);
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzPushbackPos = 0;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      <code>false</code>, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position <tt>pos</tt> from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos)
  {
    return zzBufferArray != null ? zzBufferArray[zzStartRead + pos] : zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occured while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;
    char[] zzBufferArrayL = zzBufferArray;
    char[] zzCMapL = ZZ_CMAP;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL)
            zzInput = (zzBufferArrayL != null ? zzBufferArrayL[zzCurrentPosL++] : zzBufferL.charAt(zzCurrentPosL++));
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          } else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL = zzBuffer;
            zzEndReadL = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = (zzBufferArrayL != null ? zzBufferArrayL[zzCurrentPosL++] : zzBufferL.charAt(zzCurrentPosL++));
            }
          }
          int zzNext = zzTransL[zzRowMapL[zzState] + zzCMapL[zzInput] ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          int zzAttributes = zzAttrL[zzState];
          if ((zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ((zzAttributes & 8) == 8) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction])
      {
        case 16:
        {
          return OPERATOR_DIV;
          }
        case 40:
          break;
        case 8:
        {
          return NUMBER_SIMPLE;
        }
        case 41:
          break;
        case 27:
        {
          return LEFT_BRACKET;
        }
        case 42:
          break;
        case 13:
        {
          return OPERATOR_NOT;
        }
        case 43:
          break;
        case 1:
        {
          return STRING_CONTENT;
        }
        case 44:
          break;
        case 7:
        {
          return OPERATOR_CONCAT;
        }
        case 45:
          break;
        case 15:
        {
          return SEMICOLON;
        }
        case 46:
          break;
        case 12:
        {
          return OPERATOR_COMMA;
        }
        case 47:
          break;
        case 3:
        {
          return OPERATOR_BITWISE_XOR;
        }
        case 48:
          break;
        case 35:
        {
          return NUMBER;
        }
        case 49:
          break;
        case 34:
        {
          return PACKAGE_IDENTIFIER;
        }
        case 50:
          break;
        case 37:
        {
          return SIGIL_SCALAR_INDEX;
        }
        case 51:
          break;
        case 4:
        {
          return COLON;
        }
        case 52:
          break;
        case 23:
        {
          return SIGIL_ARRAY;
        }
        case 53:
          break;
        case 19:
        {
          return OPERATOR_BITWISE_AND;
        }
        case 54:
          break;
        case 21:
        {
          return OPERATOR_BITWISE_NOT;
        }
        case 55:
          break;
        case 22:
        {
          return OPERATOR_ASSIGN;
        }
        case 56:
          break;
        case 9:
        {
          return OPERATOR_MINUS;
        }
        case 57:
          break;
        case 36:
        {
          return OPERATOR_DEREFERENCE;
        }
        case 58:
          break;
        case 17:
        {
          return OPERATOR_MUL;
        }
        case 59:
          break;
        case 24:
        {
          return SIGIL_SCALAR;
        }
        case 60:
          break;
        case 25:
        {
          return LEFT_BRACE;
        }
        case 61:
          break;
        case 10:
        {
          return OPERATOR_GT_NUMERIC;
        }
        case 62:
          break;
        case 29:
        {
          return QUOTE_TICK;
        }
        case 63:
          break;
        case 28:
        {
          return RIGHT_BRACKET;
        }
        case 64:
          break;
        case 32:
        {
          yybegin(YYINITIAL);
          return STRING_CONTENT;
        }
        case 65:
          break;
        case 11:
        {
          return OPERATOR_LT_NUMERIC;
        }
        case 66:
          break;
        case 39:
        {
          return parsePackage();
        }
        case 67:
          break;
        case 18:
        {
          return OPERATOR_MOD;
        }
        case 68:
          break;
        case 5:
        {
          return QUOTE_SINGLE;
        }
        case 69:
          break;
        case 33:
        {
          return IDENTIFIER;
        }
        case 70:
          break;
        case 38:
        {
          return parsePackageCanonical();
        }
        case 71:
          break;
        case 14:
        {
          return QUESTION;
        }
        case 72:
          break;
        case 6:
        {
          return OPERATOR_PLUS;
        }
        case 73:
          break;
        case 2:
        {
          return parseBarewordMinus();
        }
        case 74:
          break;
        case 30:
        {
          return QUOTE_DOUBLE;
        }
        case 75:
          break;
        case 26:
        {
          return RIGHT_BRACE;
        }
        case 76:
          break;
        case 31:
        {
          yybegin(LEX_ESCAPED);
          return OPERATOR_REFERENCE;
        }
        case 77:
          break;
        case 20:
        {
          return OPERATOR_BITWISE_OR;
        }
        case 78: break;
        default:
          if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
            zzAtEOF = true;
            return null;
          }
          else {
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}