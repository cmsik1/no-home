import test from 'node:test'
import assert from 'node:assert/strict'

import { parseAssistantResponse } from './agentClient.js'

test('parseAssistantResponse maps an answer payload to a text bubble', () => {
  const result = parseAssistantResponse({
    status: 200,
    ok: true,
    body: { success: true, data: { type: 'answer', answer: '마포구 평균은 13억원입니다.' } },
  })
  assert.deepEqual(result, { kind: 'answer', text: '마포구 평균은 13억원입니다.' })
})

test('parseAssistantResponse maps a command payload to a command', () => {
  const command = { action: 'search', filters: { sigungu: '강남구' } }
  const result = parseAssistantResponse({
    status: 200,
    ok: true,
    body: { success: true, data: { type: 'command', command, notice: null } },
  })
  assert.equal(result.kind, 'command')
  assert.deepEqual(result.command, command)
})

test('parseAssistantResponse maps 401/429 to errors', () => {
  assert.equal(parseAssistantResponse({ status: 401, ok: false, body: null }).kind, 'error')
  assert.equal(
    parseAssistantResponse({ status: 429, ok: false, body: { message: 'too many' }, retryAfter: '5' }).kind,
    'error',
  )
})

test('parseAssistantResponse errors when data or command is missing', () => {
  assert.equal(parseAssistantResponse({ status: 200, ok: true, body: { success: true, data: null } }).kind, 'error')
  assert.equal(
    parseAssistantResponse({ status: 200, ok: true, body: { success: true, data: { type: 'command', command: null } } }).kind,
    'error',
  )
})

test('parseAssistantResponse falls back to friendly text when answer is empty', () => {
  const result = parseAssistantResponse({
    status: 200,
    ok: true,
    body: { success: true, data: { type: 'answer', answer: '' } },
  })
  assert.equal(result.kind, 'answer')
  assert.match(result.text, /응답을 받지 못했습니다/)
})
