"""Unit tests for the haversine helpers."""
from app.utils.geo import haversine_km, path_distance_km


def test_identical_points_zero_distance():
    assert haversine_km(27.7, 85.3, 27.7, 85.3) == 0.0


def test_one_degree_longitude_at_equator():
    # ~111 km per degree of longitude at the equator
    d = haversine_km(0.0, 0.0, 0.0, 1.0)
    assert 110.0 < d < 112.0


def test_path_distance_sums_segments():
    total = path_distance_km([(0.0, 0.0), (0.0, 1.0), (0.0, 2.0)])
    assert 220.0 < total < 224.0


def test_empty_and_single_point_paths_are_zero():
    assert path_distance_km([]) == 0.0
    assert path_distance_km([(1.0, 1.0)]) == 0.0
