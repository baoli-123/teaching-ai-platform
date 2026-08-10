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
      ws: null
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
      const response = await fetch(url, options);
      const json = await response.json();
      return json.data;
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
        } else if (payload.type === 'answer') {
          this.chatMessages.push({ role: 'ai', text: payload.answer });
        }
      };
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
    await Promise.all([
      this.loadCourses(),
      this.loadDashboard(),
      this.loadRecords()
    ]);
    await Promise.all([this.loadResources(), this.loadQuestions()]);
    this.connectWebSocket();
  }
}).mount('#app');
