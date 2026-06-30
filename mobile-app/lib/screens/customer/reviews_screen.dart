import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/auth_provider.dart';
import '../../services/api_service.dart';

class ReviewsScreen extends StatefulWidget {
  final String shopId;
  final String shopName;
  final String? completedOrderId;

  const ReviewsScreen({
    super.key,
    required this.shopId,
    required this.shopName,
    this.completedOrderId,
  });

  @override
  State<ReviewsScreen> createState() => _ReviewsScreenState();
}

class _ReviewsScreenState extends State<ReviewsScreen> {
  bool _loading = true;
  List<dynamic> _reviews = [];
  String? _error;
  double _avgRating = 0.0;

  @override
  void initState() {
    super.initState();
    _loadReviews();
  }

  Future<void> _loadReviews() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final reviews = await ApiService.getShopReviews(widget.shopId);
      double avg = 0;
      if (reviews.isNotEmpty) {
        avg = reviews
                .map((r) => (r['rating'] as num?)?.toDouble() ?? 0.0)
                .reduce((a, b) => a + b) /
            reviews.length;
      }
      setState(() {
        _reviews = reviews;
        _avgRating = avg;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
        _loading = false;
      });
    }
  }

  Future<void> _showAddReviewDialog() async {
    int selectedRating = 5;
    final commentController = TextEditingController();

    await showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setLocal) => Padding(
          padding: EdgeInsets.only(
              bottom: MediaQuery.of(ctx).viewInsets.bottom,
              left: 24,
              right: 24,
              top: 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Write a Review',
                  style:
                      TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              const Text('Rating'),
              const SizedBox(height: 8),
              Row(
                children: List.generate(
                  5,
                  (i) => GestureDetector(
                    onTap: () => setLocal(() => selectedRating = i + 1),
                    child: Icon(
                      i < selectedRating ? Icons.star : Icons.star_border,
                      color: Colors.amber,
                      size: 36,
                    ),
                  ),
                ),
              ),
              const SizedBox(height: 16),
              TextField(
                controller: commentController,
                maxLines: 3,
                decoration: InputDecoration(
                  hintText: 'Share your experience...',
                  border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12)),
                  filled: true,
                  fillColor: const Color(0xFFF8F9FA),
                ),
              ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF6C63FF),
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                  ),
                  onPressed: () async {
                    if (commentController.text.trim().isEmpty) return;
                    Navigator.pop(ctx);
                    await _submitReview(
                        selectedRating, commentController.text.trim());
                  },
                  child: const Text('Submit Review',
                      style:
                          TextStyle(color: Colors.white, fontSize: 16)),
                ),
              ),
              const SizedBox(height: 24),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _submitReview(int rating, String comment) async {
    try {
      await ApiService.addReview(
        shopId: widget.shopId,
        rating: rating,
        comment: comment,
        orderId: widget.completedOrderId,
      );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
              content: Text('Review submitted!'),
              backgroundColor: Colors.green),
        );
      }
      await _loadReviews();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
              content: Text('Failed: ${e.toString()}'),
              backgroundColor: Colors.red),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        title: Text(widget.shopName,
            style: const TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.add_comment_outlined),
            onPressed: _showAddReviewDialog,
            tooltip: 'Write Review',
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: _showAddReviewDialog,
        backgroundColor: const Color(0xFF6C63FF),
        icon: const Icon(Icons.rate_review, color: Colors.white),
        label:
            const Text('Write Review', style: TextStyle(color: Colors.white)),
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? _buildError()
              : RefreshIndicator(
                  onRefresh: _loadReviews,
                  child: CustomScrollView(
                    slivers: [
                      SliverToBoxAdapter(child: _buildSummary()),
                      SliverPadding(
                        padding: const EdgeInsets.symmetric(horizontal: 16),
                        sliver: _reviews.isEmpty
                            ? SliverToBoxAdapter(child: _buildEmpty())
                            : SliverList(
                                delegate: SliverChildBuilderDelegate(
                                  (ctx, i) =>
                                      _ReviewCard(review: _reviews[i]),
                                  childCount: _reviews.length,
                                ),
                              ),
                      ),
                      const SliverToBoxAdapter(
                          child: SizedBox(height: 100)),
                    ],
                  ),
                ),
    );
  }

  Widget _buildSummary() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.06),
              blurRadius: 10,
              offset: const Offset(0, 4)),
        ],
      ),
      child: Row(
        children: [
          Column(
            children: [
              Text(
                _avgRating.toStringAsFixed(1),
                style: const TextStyle(
                    fontSize: 48, fontWeight: FontWeight.bold),
              ),
              Row(
                children: List.generate(
                  5,
                  (i) => Icon(
                    i < _avgRating.round() ? Icons.star : Icons.star_border,
                    color: Colors.amber,
                    size: 18,
                  ),
                ),
              ),
              Text('${_reviews.length} reviews',
                  style: const TextStyle(color: Colors.grey, fontSize: 12)),
            ],
          ),
          const SizedBox(width: 24),
          Expanded(
            child: Column(
              children: List.generate(5, (i) {
                final star = 5 - i;
                final count = _reviews
                    .where((r) =>
                        ((r['rating'] as num?)?.toInt() ?? 0) == star)
                    .length;
                final frac =
                    _reviews.isEmpty ? 0.0 : count / _reviews.length;
                return Padding(
                  padding: const EdgeInsets.symmetric(vertical: 2),
                  child: Row(
                    children: [
                      Text('$star',
                          style: const TextStyle(fontSize: 12)),
                      const Icon(Icons.star, color: Colors.amber, size: 12),
                      const SizedBox(width: 6),
                      Expanded(
                        child: LinearProgressIndicator(
                          value: frac,
                          backgroundColor: Colors.grey.shade200,
                          valueColor: const AlwaysStoppedAnimation(
                              Colors.amber),
                          minHeight: 6,
                        ),
                      ),
                      const SizedBox(width: 6),
                      Text('$count',
                          style: const TextStyle(
                              fontSize: 12, color: Colors.grey)),
                    ],
                  ),
                );
              }),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmpty() {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(48),
        child: Column(
          children: const [
            Icon(Icons.rate_review_outlined, size: 72, color: Colors.grey),
            SizedBox(height: 12),
            Text('No reviews yet. Be the first!',
                style: TextStyle(color: Colors.grey)),
          ],
        ),
      ),
    );
  }

  Widget _buildError() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const Icon(Icons.error_outline, size: 64, color: Colors.red),
          const SizedBox(height: 16),
          Text(_error!, textAlign: TextAlign.center),
          const SizedBox(height: 16),
          ElevatedButton(
              onPressed: _loadReviews, child: const Text('Retry')),
        ],
      ),
    );
  }
}

class _ReviewCard extends StatelessWidget {
  final dynamic review;
  const _ReviewCard({required this.review});

  @override
  Widget build(BuildContext context) {
    final rating = (review['rating'] as num?)?.toInt() ?? 0;
    final comment = review['comment'] as String? ?? '';
    final author = review['userName'] as String? ??
        review['userPhone'] as String? ??
        'Anonymous';
    final date = review['createdAt'] as String? ?? '';

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: [
          BoxShadow(
              color: Colors.black.withOpacity(0.04),
              blurRadius: 8,
              offset: const Offset(0, 2)),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                backgroundColor: const Color(0xFF6C63FF).withOpacity(0.15),
                child: Text(
                  author.isNotEmpty ? author[0].toUpperCase() : 'A',
                  style: const TextStyle(
                      color: Color(0xFF6C63FF),
                      fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(author,
                        style: const TextStyle(
                            fontWeight: FontWeight.w600)),
                    if (date.length >= 10)
                      Text(date.substring(0, 10),
                          style: const TextStyle(
                              fontSize: 12, color: Colors.grey)),
                  ],
                ),
              ),
              Row(
                children: List.generate(
                  5,
                  (i) => Icon(
                    i < rating ? Icons.star : Icons.star_border,
                    color: Colors.amber,
                    size: 16,
                  ),
                ),
              ),
            ],
          ),
          if (comment.isNotEmpty) ...
            [
              const SizedBox(height: 10),
              Text(comment,
                  style: const TextStyle(
                      color: Colors.black87, height: 1.4)),
            ],
        ],
      ),
    );
  }
}
