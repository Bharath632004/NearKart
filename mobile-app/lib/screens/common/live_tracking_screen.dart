import 'dart:async';
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

class LiveTrackingScreen extends StatefulWidget {
  final String title;
  final String orderId;
  final double sourceLat;
  final double sourceLng;
  final double destinationLat;
  final double destinationLng;
  final String courierName;
  final String vehicleInfo;

  const LiveTrackingScreen({
    super.key,
    required this.title,
    required this.orderId,
    required this.sourceLat,
    required this.sourceLng,
    required this.destinationLat,
    required this.destinationLng,
    required this.courierName,
    required this.vehicleInfo,
  });

  @override
  State<LiveTrackingScreen> createState() => _LiveTrackingScreenState();
}

class _LiveTrackingScreenState extends State<LiveTrackingScreen> {
  final Completer<GoogleMapController> _mapController = Completer();
  late LatLng _source;
  late LatLng _destination;
  late LatLng _courierPosition;
  Set<Marker> _markers = {};
  Set<Polyline> _polylines = {};
  Timer? _timer;
  double _progress = 0.0;
  String _statusText = 'Partner heading to your location';

  @override
  void initState() {
    super.initState();
    _source = LatLng(widget.sourceLat, widget.sourceLng);
    _destination = LatLng(widget.destinationLat, widget.destinationLng);
    _courierPosition = _source;
    _setMarkers();
    _setPolyline();
    _startSimulation();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _setMarkers() {
    _markers = {
      Marker(
        markerId: const MarkerId('source'),
        position: _source,
        infoWindow: const InfoWindow(title: 'Shop / Pickup'),
        icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueGreen),
      ),
      Marker(
        markerId: const MarkerId('destination'),
        position: _destination,
        infoWindow: const InfoWindow(title: 'Customer Location'),
        icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueRed),
      ),
      Marker(
        markerId: const MarkerId('courier'),
        position: _courierPosition,
        infoWindow: InfoWindow(title: widget.courierName),
        icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueAzure),
      ),
    };
  }

  void _setPolyline() {
    _polylines = {
      Polyline(
        polylineId: const PolylineId('route'),
        points: [_source, _destination],
        color: Colors.green,
        width: 5,
      )
    };
  }

  void _startSimulation() {
    _timer = Timer.periodic(const Duration(seconds: 2), (timer) async {
      if (_progress >= 1.0) {
        timer.cancel();
        if (mounted) {
          setState(() {
            _statusText = 'Order arrived';
          });
        }
        return;
      }

      setState(() {
        _progress += 0.08;
        if (_progress > 1.0) _progress = 1.0;

        final lat = _source.latitude + ((_destination.latitude - _source.latitude) * _progress);
        final lng = _source.longitude + ((_destination.longitude - _source.longitude) * _progress);
        _courierPosition = LatLng(lat, lng);

        if (_progress > 0.75) {
          _statusText = 'Partner is almost there';
        } else if (_progress > 0.4) {
          _statusText = 'Partner is on the way';
        }

        _setMarkers();
      });

      final controller = await _mapController.future;
      controller.animateCamera(CameraUpdate.newLatLng(_courierPosition));
    });
  }

  @override
  Widget build(BuildContext context) {
    final eta = (30 - (_progress * 25)).clamp(5, 30).toInt();
    return Scaffold(
      appBar: AppBar(title: Text(widget.title)),
      body: Stack(
        children: [
          GoogleMap(
            initialCameraPosition: CameraPosition(target: _source, zoom: 13.5),
            markers: _markers,
            polylines: _polylines,
            myLocationEnabled: true,
            myLocationButtonEnabled: true,
            zoomControlsEnabled: false,
            onMapCreated: (controller) {
              if (!_mapController.isCompleted) {
                _mapController.complete(controller);
              }
            },
          ),
          Positioned(
            left: 16,
            right: 16,
            top: 16,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.08),
                    blurRadius: 10,
                    offset: const Offset(0, 4),
                  )
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Order #${widget.orderId}',
                      style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                  const SizedBox(height: 6),
                  Text(_statusText, style: const TextStyle(color: Colors.grey)),
                  const SizedBox(height: 10),
                  LinearProgressIndicator(
                    value: _progress,
                    minHeight: 8,
                    borderRadius: BorderRadius.circular(10),
                    backgroundColor: Colors.grey.shade200,
                    color: Colors.green,
                  ),
                  const SizedBox(height: 10),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('ETA: $eta mins',
                          style: const TextStyle(fontWeight: FontWeight.w600)),
                      Text(widget.vehicleInfo,
                          style: const TextStyle(color: Colors.grey)),
                    ],
                  ),
                ],
              ),
            ),
          ),
          Positioned(
            left: 16,
            right: 16,
            bottom: 16,
            child: Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(18),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.08),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  )
                ],
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Row(
                    children: [
                      CircleAvatar(
                        radius: 24,
                        backgroundColor: Colors.green.shade100,
                        child: const Icon(Icons.person, color: Colors.green),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(widget.courierName,
                                style: const TextStyle(fontWeight: FontWeight.bold)),
                            Text(widget.vehicleInfo,
                                style: const TextStyle(color: Colors.grey, fontSize: 13)),
                          ],
                        ),
                      ),
                      IconButton(
                        onPressed: () {},
                        icon: const Icon(Icons.call, color: Colors.green),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () {},
                          icon: const Icon(Icons.message),
                          label: const Text('Message'),
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: ElevatedButton.icon(
                          onPressed: () {},
                          icon: const Icon(Icons.navigation),
                          label: const Text('Directions'),
                        ),
                      ),
                    ],
                  )
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
