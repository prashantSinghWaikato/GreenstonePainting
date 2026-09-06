export type ArticleBlock =
  | { type: 'heading'; text: string }
  | { type: 'paragraph'; text: string }
  | { type: 'bullets'; items: string[] }

export function parseArticleBody(body: string): ArticleBlock[] {
  const blocks: ArticleBlock[] = []
  const lines = body.replace(/\r/g, '').split('\n')
  let paragraph: string[] = []
  let bullets: string[] = []

  const flushParagraph = () => {
    const text = paragraph.join(' ').trim()
    if (text) blocks.push({ type: 'paragraph', text })
    paragraph = []
  }
  const flushBullets = () => {
    if (bullets.length) blocks.push({ type: 'bullets', items: bullets })
    bullets = []
  }

  for (const rawLine of lines) {
    const line = rawLine.trim()
    if (!line) {
      flushParagraph()
      flushBullets()
    } else if (line.startsWith('## ')) {
      flushParagraph()
      flushBullets()
      blocks.push({ type: 'heading', text: line.slice(3).trim() })
    } else if (line.startsWith('- ')) {
      flushParagraph()
      bullets.push(line.slice(2).trim())
    } else {
      flushBullets()
      paragraph.push(line)
    }
  }
  flushParagraph()
  flushBullets()
  return blocks
}
