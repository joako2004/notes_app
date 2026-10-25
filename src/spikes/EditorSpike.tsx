import { useEffect, useRef } from "react";
import type { CSSProperties } from "react";
import { basicSetup } from "codemirror";
import { markdown } from "@codemirror/lang-markdown";
import { EditorView } from "@codemirror/view";

const fieldStyle: CSSProperties = {
  width: "100%",
  minHeight: "140px",
  boxSizing: "border-box",
  padding: "10px",
  border: "1px solid #888",
  borderRadius: "6px",
  background: "#fff",
  color: "#111",
  fontFamily: "inherit",
  fontSize: "16px",
  lineHeight: 1.5,
};

const labelStyle: CSSProperties = {
  display: "block",
  marginBottom: "8px",
  fontWeight: 600,
};

function EditorSpike() {
  const editorHost = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const host = editorHost.current;
    if (!host) {
      return;
    }

    const view = new EditorView({
      doc: "# Prueba\n\nEscribí acá. ",
      extensions: [
        basicSetup,
        markdown(),
        EditorView.lineWrapping,
        EditorView.contentAttributes.of({
          autocapitalize: "sentences",
          autocorrect: "on",
          spellcheck: "true",
        }),
      ],
      parent: host,
    });

    return () => {
      view.destroy();
    };
  }, []);

  return (
    <main
      style={{
        maxWidth: "720px",
        margin: "0 auto",
        padding: "16px",
        fontFamily: "system-ui, sans-serif",
        color: "#111",
        background: "#fff",
      }}
    >
      <h1 style={{ margin: "0 0 8px", fontSize: "1.25rem" }}>
        Spike: autocorrector y mayúsculas en Android
      </h1>
      <p
        style={{
          margin: "0 0 24px",
          fontSize: "0.8rem",
          lineHeight: 1.4,
          overflowWrap: "anywhere",
        }}
      >
        navigator.language: {navigator.language} · navigator.userAgent:{" "}
        {navigator.userAgent}
      </p>

      <section style={{ marginBottom: "24px" }}>
        <label htmlFor="spike-textarea" style={labelStyle}>
          1. textarea simple
        </label>
        <textarea
          id="spike-textarea"
          autoCapitalize="sentences"
          autoCorrect="on"
          spellCheck
          style={fieldStyle}
        />
      </section>

      <section style={{ marginBottom: "24px" }}>
        <span id="spike-contenteditable-label" style={labelStyle}>
          2. contenteditable simple
        </span>
        <div
          role="textbox"
          aria-multiline="true"
          aria-labelledby="spike-contenteditable-label"
          contentEditable
          suppressContentEditableWarning
          autoCapitalize="sentences"
          autoCorrect="on"
          spellCheck
          style={fieldStyle}
        />
      </section>

      <section>
        <span id="spike-codemirror-label" style={labelStyle}>
          3. CodeMirror 6
        </span>
        <div
          ref={editorHost}
          aria-labelledby="spike-codemirror-label"
          style={{ ...fieldStyle, padding: 0, overflow: "hidden" }}
        />
      </section>
    </main>
  );
}

export default EditorSpike;
