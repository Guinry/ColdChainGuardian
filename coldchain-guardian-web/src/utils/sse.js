/**
 * Server-Sent Events (SSE) Utility for AI Assistant
 */

function getToken() {
  return localStorage.getItem('token') || sessionStorage.getItem('token');
}

export class SSEClient {
  constructor(url, options = {}) {
    const fullUrl = url.startsWith('http') ? url : `http://localhost:8080${url}`;
    const token = getToken();
    this.url = fullUrl;
    this.options = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Authorization': `Bearer ${token}`
      },
      ...options
    };
    this.onMessage = null;
    this.onError = null;
    this.onDone = null;
    this.controller = null;
    this.hasReceivedData = false;
    this.isFinished = false;
  }

  finish() {
    if (this.isFinished) return;
    this.isFinished = true;
    if (this.onDone) this.onDone();
  }

  isAbortLikeError(error) {
    if (error?.name === 'AbortError') return true;
    if (this.controller?.signal?.aborted) return true;
    const message = String(error?.message || '').toLowerCase();
    return message.includes('aborted') || message.includes('abort');
  }

  isBenignStreamClose(error) {
    if (!this.hasReceivedData) return false;
    if (error instanceof TypeError) {
      const message = String(error?.message || '').toLowerCase();
      return message.includes('network') || message.includes('failed to fetch') || message.includes('load failed');
    }
    return false;
  }

  connect(body = null) {
    this.controller = new AbortController();
    this.hasReceivedData = false;
    this.isFinished = false;

    const options = {
      ...this.options,
      body: body ? JSON.stringify(body) : undefined,
      signal: this.controller.signal
    };

    fetch(this.url, options)
        .then(async response => {
          if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

          const reader = response.body.getReader();
          const decoder = new TextDecoder('utf-8');
          let buffer = '';

          while (true) {
            const { done, value } = await reader.read();
            if (done) {
              if (this.onDone) this.onDone();
              break;
            }

            // 将新数据追加到 buffer，stream: true 保证中文字符不被截断
            buffer += decoder.decode(value, { stream: true });

            // 🌟 核心修复：严格按照 SSE 规范，用双换行符 \n\n 拆分事件包
            let match;
            while ((match = /\r?\n\r?\n/.exec(buffer)) !== null) {
              // 提取出一个完整的事件包
              const eventString = buffer.slice(0, match.index);
              // 将处理过的部分从 buffer 中移除
              buffer = buffer.slice(match.index + match[0].length);

              if (eventString.trim() === '') continue;

              // 处理这个事件包内的所有行
              const lines = eventString.split(/\r?\n/);
              let dataLines = [];

              for (const line of lines) {
                if (line.startsWith('data:')) {
                  let text = line.slice(5);
                  // 规范：去除紧跟在冒号后面的一个空格
                  if (text.startsWith(' ')) text = text.slice(1);
                  dataLines.push(text);
                }
              }

              // 如果有数据，发送给前端
              if (dataLines.length > 0) {
                // 🌟 核心：将同一事件内的多行数据用换行符重新拼接！完美恢复原本的排版！
                const dataStr = dataLines.join('\n');

                if (dataStr === '[DONE]') {
                  this.finish();
                  return; // 结束流
                }

                try {
                  // 尝试解析 JSON（以防后端未来改成 JSON 格式）
                  const parsed = JSON.parse(dataStr);
                  this.hasReceivedData = true;
                  if (this.onMessage) this.onMessage(parsed);
                } catch (e) {
                  // 如果是普通纯文本，直接带上完整的换行符推给前端
                  if (this.onMessage) {
                    this.hasReceivedData = true;
                    this.onMessage({ content: dataStr, type: 'token' });
                  }
                }
              }
            }
          }
        })
        .catch(error => {
          if (this.isAbortLikeError(error)) {
            return;
          }
          if (this.isBenignStreamClose(error)) {
            this.finish();
            return;
          }
          if (this.onError) {
            this.onError(error);
          }
        });

    return this.controller;
  }

  setMessageHandler(handler) { this.onMessage = handler; }
  setErrorHandler(handler) { this.onError = handler; }
  setDoneHandler(handler) { this.onDone = handler; }
  abort() { if (this.controller) this.controller.abort(); }
}
