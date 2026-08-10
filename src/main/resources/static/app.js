const { createApp } = Vue;

createApp({
  data() {
    return {
      tabs: [
        { key: 'resources', label: '课程资源' },
        { key: 'chat', label: 'AI 问答' },
        { key: 'exam', label: '在线考试' },
        { key: 'dashboard', label: '学情看板' }
      ],
      activeTab: 'resources',
      courses: [],
      courseId: 1,
      resources: [],
      questions: [],
      chatMessages: [],
      chatInput: '',
      studentName: '',
      examAnswers: {},
      examResult: null,
      stats: {},
      records: [],
      connected: false,
      ws: null,
      user: null,
      loginUsername: 'student',
      loginPassword: 'student123',
      token: localStorage.getItem('teaching_token') || ''
    };
  },
  computed: {
    statItems() {
      return [
        { label: '课程', value: this.stats.courseCount || 0 },
        { label: '资源', value: this.stats.resourceCount || 0 },
        { label: '题库', value: this.stats.questionCount || 0 },
        { label: '问答记录', value: this.stats.chatCount || 0 },
        { label: '考试次数', value: this.stats.examCount || 0 },
        { label: '平均得分', value: this.stats.avgScore || 0 }
      ];
    }
  },
  methods: {
    async api(url, options = {}) {
      const headers = { ...(options.headers || {}) };
      if (this.token) {
        headers.Authorization = `Bearer ${this.token}`;
      }
      const response = await fetch(url, { ...options, headers });
      if (response.status === 401 && !url.includes('/api/auth/login')) {
        this.user = null;
        this.token = '';
        localStorage.removeItem('teaching_token');
        throw new Error('unauthorized');
      }
      const json = await response.json();
      return json.data;
    },
    async login() {
      const data = await this.api('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: this.loginUsername, password: this.loginPassword })
      });
      this.token = data.accessToken;
      localStorage.setItem('teaching_token', data.accessToken);
      this.user = { username: data.username, displayName: data.displayName, role: data.role };
      await this.initialize();
    },
    async logout() {
      try {
        await this.api('/api/auth/logout', { method: 'POST' });
      } catch (ignored) {
        // Local logout still proceeds when the remote session is invalid.
      }
      this.token = '';
      this.user = null;
      localStorage.removeItem('teaching_token');
      if (this.ws) {
        this.ws.close();
      }
    },
    async setLocale(lang) {
      await this.api(`/api/locale?lang=${lang}`);
    },
    async loadCourses() {
      this.courses = await this.api('/api/courses');
    },
    async loadResources() {
      this.resources = await this.api(`/api/courses/${this.courseId}/resources`);
    },
    async loadQuestions() {
      this.questions = await this.api(`/api/courses/${this.courseId}/questions`);
      this.examAnswers = {};
      this.examResult = null;
    },
    async loadDashboard() {
      this.stats = await this.api('/api/dashboard/stats');
    },
    async loadRecords() {
      this.records = await this.api('/api/exams/records');
    },
    async switchTab(tab) {
      this.activeTab = tab;
      if (tab === 'resources') {
        await this.loadResources();
      } else if (tab === 'exam') {
        await this.loadQuestions();
      } else if (tab === 'dashboard') {
        await Promise.all([this.loadDashboard(), this.loadRecords()]);
      }
    },
    options(question) {
      return [
        { key: 'A', label: question.optionA },
        { key: 'B', label: question.optionB },
        { key: 'C', label: question.optionC },
        { key: 'D', label: question.optionD }
      ].filter((item) => item.label);
    },
    async submitExam() {
      this.examResult = await this.api('/api/exams/submit', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          studentName: this.studentName || '匿名学员',
          courseId: this.courseId,
          answers: this.examAnswers
        })
      });
      await Promise.all([this.loadDashboard(), this.loadRecords()]);
    },
    connectWebSocket() {
      const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
      this.ws = new WebSocket(`${protocol}://${location.host}/ws/chat`);
      this.ws.onopen = () => {
        this.connected = true;
      };
      this.ws.onclose = () => {
        this.connected = false;
      };
      this.ws.onmessage = (event) => {
        const payload = JSON.parse(event.data);
        if (payload.type === 'status') {
          this.chatMessages.push({ role: 'ai', text: payload.message });
        } else if (payload.type === 'chunk') {
          const last = this.chatMessages[this.chatMessages.length - 1];
          if (last && last.role === 'ai' && last.streaming) {
            last.text += payload.content;
          } else {
            this.chatMessages.push({ role: 'ai', text: payload.content, streaming: true });
          }
        } else if (payload.type === 'answer') {
          const last = this.chatMessages[this.chatMessages.length - 1];
          if (last && last.role === 'ai') {
            last.text = payload.answer;
            last.streaming = false;
          } else {
            this.chatMessages.push({ role: 'ai', text: payload.answer });
          }
        }
      };
    },
    async initialize() {
      await Promise.all([
        this.loadCourses(),
        this.loadDashboard(),
        this.loadRecords()
      ]);
      await Promise.all([this.loadResources(), this.loadQuestions()]);
      this.connectWebSocket();
    },
    async sendChat() {
      const question = this.chatInput.trim();
      if (!question) {
        return;
      }
      this.chatMessages.push({ role: 'user', text: question });
      this.chatInput = '';

      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify({ question, courseId: this.courseId }));
        return;
      }

      const result = await this.api('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question, courseId: this.courseId })
      });
      this.chatMessages.push({ role: 'ai', text: result.answer });
      await this.loadDashboard();
    }
  },
  async mounted() {
    if (this.token) {
      try {
        const me = await this.api('/api/auth/me');
        this.user = me;
        await this.initialize();
      } catch (ignored) {
        this.user = null;
        this.token = '';
        localStorage.removeItem('teaching_token');
      }
    }
  }
}).mount('#app');
