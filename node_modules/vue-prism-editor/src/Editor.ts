import Vue, { VNode } from 'vue';

import './styles.css';
const KEYCODE_ENTER = 13;
const KEYCODE_TAB = 9;
const KEYCODE_BACKSPACE = 8;
const KEYCODE_Y = 89;
const KEYCODE_Z = 90;
const KEYCODE_M = 77;
const KEYCODE_PARENS = 57;
const KEYCODE_BRACKETS = 219;
const KEYCODE_QUOTE = 222;
const KEYCODE_BACK_QUOTE = 192;
const KEYCODE_ESCAPE = 27;

const HISTORY_LIMIT = 100;
const HISTORY_TIME_GAP = 3000;

const isWindows = typeof window !== 'undefined' && navigator && /Win/i.test(navigator.platform);
const isMacLike = typeof window !== 'undefined' && navigator && /(Mac|iPhone|iPod|iPad)/i.test(navigator.platform);

export interface EditorProps {
  lineNumbers: boolean;
  autoStyleLineNumbers: boolean;
  readonly: boolean;
  value: string;
  highlight: () => string;
  tabSize: number;
  insertSpaces: boolean;
  ignoreTabKey: boolean;
  placeholder: string;
}
export interface Record {
  value: string;
  selectionStart: number;
  selectionEnd: number;
}

export interface History {
  stack: Array<Record & { timestamp: number }>;
  offset: number;
}

export const PrismEditor = Vue.extend({
  props: {
    lineNumbers: {
      type: Boolean,
      default: false,
    },
    autoStyleLineNumbers: {
      type: Boolean,
      default: true,
    },
    readonly: {
      type: Boolean,
      default: false,
    },
    value: {
      type: String,
      default: '',
    },
    highlight: {
      type: Function,
      required: true,
    },
    tabSize: {
      type: Number,
      default: 2,
    },
    insertSpaces: {
      type: Boolean,
      default: true,
    },
    ignoreTabKey: {
      type: Boolean,
      default: false,
    },
    placeholder: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      capture: true,
      history: {
        stack: [],
        offset: -1,
      } as History,
      lineNumbersHeight: '20px',
      codeData: '',
    };
  },
  watch: {
    value: {
      immediate: true,
      handler(newVal: string): void {
        if (!newVal) {
          this.codeData = '';
        } else {
          this.codeData = newVal;
        }
      },
    },
    content: {
      immediate: true,
      handler(): void {
        if (this.lineNumbers) {
          this.$nextTick(() => {
            this.setLineNumbersHeight();
          });
        }
      },
    },
    lineNumbers(): void {
      this.$nextTick(() => {
        this.styleLineNumbers();
        this.setLineNumbersHeight();
      });
    },
  },
  computed: {
    isEmpty(): boolean {
      return this.codeData.length === 0;
    },
    content(): string {
      const result = this.highlight(this.codeData) + '<br />';
      // todo: VNode support?
      return result;
    },
    lineNumbersCount(): number {
      const totalLines = this.codeData.split(/\r\n|\n/).length;
      return totalLines;
    },
  },
  mounted() {
    this._recordCurrentState();
    this.styleLineNumbers();
  },

  methods: {
    setLineNumbersHeight(): void {
      this.lineNumbersHeight = getComputedStyle(this.$refs.pre as HTMLTextAreaElement).height;
    },
    styleLineNumbers(): void {
      if (!this.lineNumbers || !this.autoStyleLineNumbers) return;

      const $editor = this.$refs.pre as HTMLTextAreaElement;
      const $lineNumbers: HTMLDivElement | null = this.$el.querySelector('.prism-editor__line-numbers');
      const editorStyles = window.getComputedStyle($editor);

      this.$nextTick(() => {
        const btlr: any = 'border-top-left-radius';
        const bblr: any = 'border-bottom-left-radius';
        if (!$lineNumbers) return;
        $lineNumbers.style[btlr] = editorStyles[btlr];
        $lineNumbers.style[bblr] = editorStyles[bblr];
        $editor.style[btlr] = '0';
        $editor.style[bblr] = '0';

        const stylesList = ['background-color', 'margin-top', 'padding-top', 'font-family', 'font-size', 'line-height'];
        stylesList.forEach((style: any) => {
          $lineNumbers.style[style] = editorStyles[style];
        });
        $lineNumbers.style['margin-bottom' as any] = '-' + editorStyles['padding-top' as any];
      });
    },
    _recordCurrentState(): void {
      const input = this.$refs.textarea as HTMLTextAreaElement;

      if (!input) return;
      // Save current state of the input
      const { value, selectionStart, selectionEnd } = input;

      this._recordChange({
        value,
        selectionStart,
        selectionEnd,
      });
    },
    _getLines(text: string, position: number): Array<string> {
      return text.substring(0, position).split('\n');
    },
    _applyEdits(record: Record): void {
      // Save last selection state
      const input = this.$refs.textarea as HTMLTextAreaElement;
      const last = this.history.stack[this.history.offset];

      if (last && input) {
        this.history.stack[this.history.offset] = {
          ...last,
          selectionStart: input.selectionStart,
          selectionEnd: input.selectionEnd,
        };
      }

      // Save the changes
      this._recordChange(record);
      this._updateInput(record);
    },
    _recordChange(record: Record, overwrite = false): void {
      const { stack, offset } = this.history;

      if (stack.length && offset > -1) {
        // When something updates, drop the redo operations
        this.history.stack = stack.slice(0, offset + 1);

        // Limit the number of operations to 100
        const count = this.history.stack.length;

        if (count > HISTORY_LIMIT) {
          const extras = count - HISTORY_LIMIT;

          this.history.stack = stack.slice(extras, count);
          this.history.offset = Math.max(this.history.offset - extras, 0);
        }
      }

      const timestamp = Date.now();

      if (overwrite) {
        const last = this.history.stack[this.history.offset];

        if (last && timestamp - last.timestamp < HISTORY_TIME_GAP) {
          // A previous entry exists and was in short interval

          // Match the last word in the line
          const re = /[^a-z0-9]([a-z0-9]+)$/i;

          // Get the previous line
          const previous = this._getLines(last.value, last.selectionStart).pop()?.match(re);

          // Get the current line
          const current = this._getLines(record.value, record.selectionStart).pop()?.match(re);

          if (previous && current && current[1].startsWith(previous[1])) {
            // The last word of the previous line and current line match
            // Overwrite previous entry so that undo will remove whole word
            this.history.stack[this.history.offset] = {
              ...record,
              timestamp,
            };

            return;
          }
        }
      }

      // Add the new operation to the stack
      this.history.stack.push({ ...record, timestamp });
      this.history.offset++;
    },

    _updateInput(record: Record): void {
      const input = this.$refs.textarea as HTMLTextAreaElement;

      if (!input) return;

      // Update values and selection state
      input.value = record.value;
      input.selectionStart = record.selectionStart;
      input.selectionEnd = record.selectionEnd;

      this.$emit('input', record.value);
      // this.props.onValueChange(record.value);
    },
    handleChange(e: KeyboardEvent): void {
      const { value, selectionStart, selectionEnd } = e.target as HTMLTextAreaElement;

      this._recordChange(
        {
          value,
          selectionStart,
          selectionEnd,
        },
        true
      );
      this.$emit('input', value);
      // this.props.onValueChange(value);
    },
    _undoEdit(): void {
      const { stack, offset } = this.history;

      // Get the previous edit
      const record = stack[offset - 1];

      if (record) {
        // Apply the changes and update the offset
        this._updateInput(record);
        this.history.offset = Math.max(offset - 1, 0);
      }
    },
    _redoEdit(): void {
      const { stack, offset } = this.history;

      // Get the next edit
      const record = stack[offset + 1];

      if (record) {
        // Apply the changes and update the offset
        this._updateInput(record);
        this.history.offset = Math.min(offset + 1, stack.length - 1);
      }
    },
    handleKeyDown(e: KeyboardEvent): void {
      // console.log(navigator.platform);
      const { tabSize, insertSpaces, ignoreTabKey } = this;

      if (this.$listeners.keydown) {
        // onKeyDown(e);
        this.$emit('keydown', e);

        if (e.defaultPrevented) {
          return;
        }
      }

      if (e.keyCode === KEYCODE_ESCAPE) {
        (<HTMLTextAreaElement>e.target).blur();
        this.$emit('blur', e);
      }

      const { value, selectionStart, selectionEnd } = e.target as HTMLTextAreaElement;

      const tabCharacter = (insertSpaces ? ' ' : '\t').repeat(tabSize);

      if (e.keyCode === KEYCODE_TAB && !ignoreTabKey && this.capture) {
        // Prevent focus change
        e.preventDefault();

        if (e.shiftKey) {
          // Unindent selected lines
          const linesBeforeCaret = this._getLines(value, selectionStart);
          const startLine = linesBeforeCaret.length - 1;
          const endLine = this._getLines(value, selectionEnd).length - 1;
          const nextValue = value
            .split('\n')
            .map((line, i) => {
              if (i >= startLine && i <= endLine && line.startsWith(tabCharacter)) {
                return line.substring(tabCharacter.length);
              }

              return line;
            })
            .join('\n');

          if (value !== nextValue) {
            const startLineText = linesBeforeCaret[startLine];

            this._applyEdits({
              value: nextValue,
              // Move the start cursor if first line in selection was modified
              // It was modified only if it started with a tab
              selectionStart: startLineText.startsWith(tabCharacter)
                ? selectionStart - tabCharacter.length
                : selectionStart,
              // Move the end cursor by total number of characters removed
              selectionEnd: selectionEnd - (value.length - nextValue.length),
            });
          }
        } else if (selectionStart !== selectionEnd) {
          // Indent selected lines
          const linesBeforeCaret = this._getLines(value, selectionStart);
          const startLine = linesBeforeCaret.length - 1;
          const endLine = this._getLines(value, selectionEnd).length - 1;
          const startLineText = linesBeforeCaret[startLine];

          this._applyEdits({
            value: value
              .split('\n')
              .map((line, i) => {
                if (i >= startLine && i <= endLine) {
                  return tabCharacter + line;
                }

                return line;
              })
              .join('\n'),
            // Move the start cursor by number of characters added in first line of selection
            // Don't move it if it there was no text before cursor
            selectionStart: /\S/.test(startLineText) ? selectionStart + tabCharacter.length : selectionStart,
            // Move the end cursor by total number of characters added
            selectionEnd: selectionEnd + tabCharacter.length * (endLine - startLine + 1),
          });
        } else {
          const updatedSelection = selectionStart + tabCharacter.length;

          this._applyEdits({
            // Insert tab character at caret
            value: value.substring(0, selectionStart) + tabCharacter + value.substring(selectionEnd),
            // Update caret position
            selectionStart: updatedSelection,
            selectionEnd: updatedSelection,
          });
        }
      } else if (e.keyCode === KEYCODE_BACKSPACE) {
        const hasSelection = selectionStart !== selectionEnd;
        const textBeforeCaret = value.substring(0, selectionStart);

        if (textBeforeCaret.endsWith(tabCharacter) && !hasSelection) {
          // Prevent default delete behaviour
          e.preventDefault();

          const updatedSelection = selectionStart - tabCharacter.length;

          this._applyEdits({
            // Remove tab character at caret
            value: value.substring(0, selectionStart - tabCharacter.length) + value.substring(selectionEnd),
            // Update caret position
            selectionStart: updatedSelection,
            selectionEnd: updatedSelection,
          });
        }
      } else if (e.keyCode === KEYCODE_ENTER) {
        // Ignore selections
        if (selectionStart === selectionEnd) {
          // Get the current line
          const line = this._getLines(value, selectionStart).pop();
          const matches = line?.match(/^\s+/);

          if (matches && matches[0]) {
            e.preventDefault();

            // Preserve indentation on inserting a new line
            const indent = '\n' + matches[0];
            const updatedSelection = selectionStart + indent.length;

            this._applyEdits({
              // Insert indentation character at caret
              value: value.substring(0, selectionStart) + indent + value.substring(selectionEnd),
              // Update caret position
              selectionStart: updatedSelection,
              selectionEnd: updatedSelection,
            });
          }
        }
      } else if (
        e.keyCode === KEYCODE_PARENS ||
        e.keyCode === KEYCODE_BRACKETS ||
        e.keyCode === KEYCODE_QUOTE ||
        e.keyCode === KEYCODE_BACK_QUOTE
      ) {
        let chars;

        if (e.keyCode === KEYCODE_PARENS && e.shiftKey) {
          chars = ['(', ')'];
        } else if (e.keyCode === KEYCODE_BRACKETS) {
          if (e.shiftKey) {
            chars = ['{', '}'];
          } else {
            chars = ['[', ']'];
          }
        } else if (e.keyCode === KEYCODE_QUOTE) {
          if (e.shiftKey) {
            chars = ['"', '"'];
          } else {
            chars = ["'", "'"];
          }
        } else if (e.keyCode === KEYCODE_BACK_QUOTE && !e.shiftKey) {
          chars = ['`', '`'];
        }

        // console.log(isMacLike, "navigator" in global && /(Mac|iPhone|iPod|iPad)/i.test(navigator.platform));

        // If text is selected, wrap them in the characters
        if (selectionStart !== selectionEnd && chars) {
          e.preventDefault();

          this._applyEdits({
            value:
              value.substring(0, selectionStart) +
              chars[0] +
              value.substring(selectionStart, selectionEnd) +
              chars[1] +
              value.substring(selectionEnd),
            // Update caret position
            selectionStart,
            selectionEnd: selectionEnd + 2,
          });
        }
      } else if (
        (isMacLike
          ? // Trigger undo with ⌘+Z on Mac
            e.metaKey && e.keyCode === KEYCODE_Z
          : // Trigger undo with Ctrl+Z on other platforms
            e.ctrlKey && e.keyCode === KEYCODE_Z) &&
        !e.shiftKey &&
        !e.altKey
      ) {
        e.preventDefault();

        this._undoEdit();
      } else if (
        (isMacLike
          ? // Trigger redo with ⌘+Shift+Z on Mac
            e.metaKey && e.keyCode === KEYCODE_Z && e.shiftKey
          : isWindows
          ? // Trigger redo with Ctrl+Y on Windows
            e.ctrlKey && e.keyCode === KEYCODE_Y
          : // Trigger redo with Ctrl+Shift+Z on other platforms
            e.ctrlKey && e.keyCode === KEYCODE_Z && e.shiftKey) &&
        !e.altKey
      ) {
        e.preventDefault();

        this._redoEdit();
      } else if (e.keyCode === KEYCODE_M && e.ctrlKey && (isMacLike ? e.shiftKey : true)) {
        e.preventDefault();

        // Toggle capturing tab key so users can focus away
        this.capture = !this.capture;
      }
    },
  },
  render(h): VNode {
    const lineNumberWidthCalculator = h(
      'div',
      {
        attrs: {
          class: 'prism-editor__line-width-calc',
          style: 'height: 0px; visibility: hidden; pointer-events: none;',
        },
      },
      '999'
    );
    const lineNumbers = h(
      'div',
      {
        staticClass: 'prism-editor__line-numbers',
        style: {
          'min-height': this.lineNumbersHeight,
        },
        attrs: {
          'aria-hidden': 'true',
        },
      },
      [
        lineNumberWidthCalculator,
        Array.from(Array(this.lineNumbersCount).keys()).map((_, index) => {
          return h('div', { attrs: { class: 'prism-editor__line-number token comment' } }, `${++index}`);
        }),
      ]
    );

    const textarea = h('textarea', {
      ref: 'textarea',
      on: {
        input: this.handleChange,
        keydown: this.handleKeyDown,
        click: ($event: MouseEvent) => {
          this.$emit('click', $event);
        },
        keyup: ($event: KeyboardEvent) => {
          this.$emit('keyup', $event);
        },
        focus: ($event: FocusEvent) => {
          this.$emit('focus', $event);
        },
        blur: ($event: FocusEvent) => {
          this.$emit('blur', $event);
        },
      },
      staticClass: 'prism-editor__textarea',
      class: {
        'prism-editor__textarea--empty': this.isEmpty,
      },
      attrs: {
        spellCheck: 'false',
        autocapitalize: 'off',
        autocomplete: 'off',
        autocorrect: 'off',
        'data-gramm': 'false',
        placeholder: this.placeholder,
        'data-testid': 'textarea',
        readonly: this.readonly,
      },
      domProps: {
        value: this.codeData,
      },
    });
    const preview = h('pre', {
      ref: 'pre',
      staticClass: 'prism-editor__editor',
      attrs: {
        'data-testid': 'preview',
      },
      domProps: {
        innerHTML: this.content,
      },
    });
    const editorContainer = h('div', { staticClass: 'prism-editor__container' }, [textarea, preview]);
    return h('div', { staticClass: 'prism-editor-wrapper' }, [this.lineNumbers && lineNumbers, editorContainer]);
  },
});
