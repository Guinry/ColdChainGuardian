/**
 * Server-Sent Events (SSE) Utility for AI Assistant
 */

export class SSEClient {
  constructor(url, options = {}) {
    this.url = url;
    this.options = {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/event-stream',
        'Cache-Control': 'no-cache'
      },
      ...options
    };
    this.eventSource = null;
    this.onMessage = null;
    this.onError = null;
    this.onOpen = null;
  }

  connect(body = null) {
    // Since EventSource doesn't support POST requests with body,
    // we use fetch with ReadableStream to handle SSE
    const controller = new AbortController();

    const options = {
      ...this.options,
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal
    };

    fetch(this.url, options)
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        if (!response.body) {
          throw new Error('ReadableStream not yet supported in this browser.');
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        const readStream = () => {
          reader.read().then(({ done, value }) => {
            if (done) {
              return;
            }

            buffer += decoder.decode(value, { stream: true });

            // Process complete lines
            const lines = buffer.split('\n');
            buffer = lines.pop(); // Keep incomplete line in buffer

            for (const line of lines) {
              if (line.startsWith('data: ')) {
                const data = line.substring(6); // Remove 'data: ' prefix

                if (data === '[DONE]') {
                  // Stream completed
                  return;
                }

                try {
                  // Try parsing as JSON first
                  const parsedData = JSON.parse(data);
                  if (this.onMessage) {
                    this.onMessage(parsedData);
                  }
                } catch (e) {
                  // If not JSON, treat as plain text
                  if (this.onMessage && data.trim()) {
                    this.onMessage({ content: data, type: 'token' });
                  }
                }
              } else if (line.startsWith('event: ')) {
                // Handle custom event types if needed
                const eventType = line.substring(7);
                if (eventType === 'open' && this.onOpen) {
                  this.onOpen();
                }
              }
            }

            readStream(); // Continue reading
          }).catch(error => {
            if (this.onError) {
              this.onError(error);
            }
          });
        };

        readStream(); // Start reading
      })
      .catch(error => {
        if (this.onError) {
          this.onError(error);
        }
      });

    return controller; // Return controller to allow cancellation
  }

  setMessageHandler(handler) {
    this.onMessage = handler;
  }

  setErrorHandler(handler) {
    this.onError = handler;
  }

  setOpenHandler(handler) {
    this.onOpen = handler;
  }

  close() {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}